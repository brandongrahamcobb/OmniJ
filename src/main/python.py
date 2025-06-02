import os
import re

# Root directory to start search
ROOT_DIR = "./"

# File extensions to include
ALLOWED_EXTENSIONS = {'.txt', '.java', '.py', '.md'}

# Pattern to find `(`
pattern = re.compile(r'\(th\.')

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        original = f.read()

    fixed = pattern.sub('(', original)

    if fixed != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(fixed)
        print(f"Reverted: {filepath}")

def walk_and_fix(root):
    for dirpath, _, filenames in os.walk(root):
        for filename in filenames:
            _, ext = os.path.splitext(filename)
            if ext.lower() in ALLOWED_EXTENSIONS:
                fix_file(os.path.join(dirpath, filename))

if __name__ == "__main__":
    walk_and_fix(ROOT_DIR)
