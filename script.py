"""
upload_to_openai.py

Overwrite your OpenAI file-based vector store (purpose="user_data") by:
  1) Deleting all existing files with that purpose
  2) Uploading only supported files from a directory
     • Text files (.c/.py/.java/etc) get merged into one JSONL
     • Binary files (.pdf/.docx/.pptx) get uploaded individually
  3) Or uploading a single existing JSONL file as-is

Usage:
  pip install openai
  export OPENAI_API_KEY=sk-…
  python3 upload_to_openai.py --dir path/to/my/code
  python3 upload_to_openai.py --file my_bundle.jsonl
"""

import os, sys, argparse, json, tempfile

try:
    import openai
    from openai import OpenAI
    NEW_SDK = True
except ImportError:
    print("Error: please install openai (`pip install openai`)", file=sys.stderr)
    sys.exit(1)
except Exception:
    NEW_SDK = False

SUPPORTED_EXT = {
    ".c", ".cpp", ".cs", ".css", ".go", ".html", ".java",
    ".js", ".json", ".md", ".py", ".rb", ".sh", ".tex",
    ".ts", ".txt", ".doc", ".docx", ".pdf", ".pptx"
}
BINARY_EXTS = {".pdf", ".doc", ".docx", ".pptx"}


def parse_args():
    p = argparse.ArgumentParser(
        description="Overwrite your OpenAI file store with supported files only"
    )
    grp = p.add_mutually_exclusive_group(required=True)
    grp.add_argument("-d", "--dir",  help="Directory of supported files")
    grp.add_argument("-f", "--file", help="Path to an existing JSONL file")
    p.add_argument("-p", "--purpose", default="user_data",
                   help="OpenAI file purpose (default: user_data)")
    p.add_argument("-k", "--api-key",
                   help="OpenAI API Key (or set OPENAI_API_KEY env var)")
    return p.parse_args()


def init_client(key):
    if NEW_SDK:
        return OpenAI(api_key=key)
    openai.api_key = key
    return openai


def list_remote_files(client, purpose):
    """
    Return all remote files whose .purpose == purpose.
    Works for both new SDK (FileObject with attributes) and old dict responses.
    """
    # fetch raw list
    if NEW_SDK:
        resp = client.files.list()
        items = getattr(resp, "data", []) or resp.get("data", [])
    elif hasattr(openai, "File") and hasattr(openai.File, "list"):
        resp = openai.File.list()
        items = resp.get("data", [])
    else:
        resp = openai.files.list()
        items = resp.get("data", [])

    # filter by purpose
    out = []
    for i in items:
        # try attribute first, then dict lookup
        p = getattr(i, "purpose", None)
        if p is None and isinstance(i, dict):
            p = i.get("purpose")
        if p == purpose:
            out.append(i)
    return out


def delete_file(client, f):
    """
    Delete a remote file. Accepts either a FileObject or a dict.
    """
    # pull the id
    fid = getattr(f, "id", None)
    if fid is None and isinstance(f, dict):
        fid = f.get("id")
    if not fid:
        raise RuntimeError(f"Cannot determine file id from {f!r}")

    # dispatch delete call correctly
    if NEW_SDK:
        # positional or `file=` both work; avoid `id=`
        client.files.delete(fid)
    elif hasattr(openai, "File") and hasattr(openai.File, "delete"):
        # old v0.28 interface
        openai.File.delete(fid)
    else:
        # fallback module path
        openai.files.delete(fid)

    print(f"Deleted remote file {fid}")

def purge_remote_store(client, purpose):
    for f in list_remote_files(client, purpose):
        delete_file(client, f)


def make_jsonl_from_texts(dirpath):
    tmp = tempfile.NamedTemporaryFile(
        "w", delete=False, suffix=".jsonl", encoding="utf-8"
    )
    for root, _, files in os.walk(dirpath):
        for fn in sorted(files):
            ext = os.path.splitext(fn)[1].lower()
            if ext not in (SUPPORTED_EXT - BINARY_EXTS):
                continue
            path = os.path.join(root, fn)
            text = open(path, "r", encoding="utf-8", errors="ignore").read().strip()
            if not text:
                continue
            obj = {"text": text,
                   "metadata": {"path": os.path.relpath(path, dirpath)}}
            json.dump(obj, tmp)
            tmp.write("\n")
    tmp.close()
    return tmp.name


def upload(client, path, purpose):
    f = open(path, "rb")
    if NEW_SDK:
        return client.files.create(file=f, purpose=purpose)
    if hasattr(openai, "File") and hasattr(openai.File, "create"):
        return openai.File.create(file=f, purpose=purpose)
    return openai.files.create(file=f, purpose=purpose)


def main():
    args = parse_args()
    key = args.api_key or os.getenv("OPENAI_API_KEY")
    if not key:
        print("Error: missing API key", file=sys.stderr)
        sys.exit(1)

    client = init_client(key)
    purge_remote_store(client, args.purpose)

    to_upload = []
    tmp_cleanup = []

    if args.file:
        if not os.path.isfile(args.file):
            print(f"Error: {args.file} not found", file=sys.stderr)
            sys.exit(1)
        to_upload.append(args.file)
    else:
        if not os.path.isdir(args.dir):
            print(f"Error: {args.dir} is not a directory", file=sys.stderr)
            sys.exit(1)
        # batch text files
        jl = make_jsonl_from_texts(args.dir)
        to_upload.append(jl)
        tmp_cleanup.append(jl)
        # each binary file separately
        for root, _, files in os.walk(args.dir):
            for fn in sorted(files):
                ext = os.path.splitext(fn)[1].lower()
                if ext in BINARY_EXTS:
                    to_upload.append(os.path.join(root, fn))

    for pth in to_upload:
        print(f"Uploading {pth!r} …")
        try:
            r = upload(client, pth, args.purpose)
        except Exception as e:
            print("Upload failed:", e, file=sys.stderr)
            sys.exit(1)
        fid = getattr(r, "id", None) or r.get("id")
        if fid:
            print("→ File ID:", fid)
        else:
            print("Warning: no file ID returned", file=sys.stderr)

    for tmpf in tmp_cleanup:
        try: os.remove(tmpf)
        except: pass


if __name__ == "__main__":
    main()
