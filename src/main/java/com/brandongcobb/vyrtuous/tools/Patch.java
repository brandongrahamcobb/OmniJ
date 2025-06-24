//
//  Patch.java
//  
//
//  Created by Brandon Cobb on 6/24/25.
//
package com.brandongcobb.vyrtuous.tools;

import com.brandongcobb.vyrtuous.domain.*;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.objects.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Patch implements Tool<PatchInput, PatchStatus> {
    
    ContextManager contextManager;
    
    @Override
    public String getName() {
        return "patch";
    }
    
    public Patch(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public PatchStatus run(PatchInput input) {
        
        contextManager.addEntry(new ContextEntry(ContextEntry.Type.TOOL, input.getOriginalJson().asText()));
        String path = input.getPath();
        String patch = input.getPatch();
        String mode = input.getMode() != null ? input.getMode() : "replace";

        try {
            Path filePath = Path.of(path);
            List<String> originalLines = Files.readAllLines(filePath);
            List<String> patchedLines;

            if ("replace".equalsIgnoreCase(mode)) {
                patchedLines = applyLineReplacePatch(originalLines, patch);
            } else {
                return new PatchStatus(false, "Unsupported patch mode: " + mode);
            }

            Files.write(filePath, patchedLines);
            return new PatchStatus(true, "Patch applied successfully.");
        } catch (IOException e) {
            return new PatchStatus(false, "IO error: " + e.getMessage());
        } catch (Exception e) {
            return new PatchStatus(false, "Unexpected error: " + e.getMessage());
        }
    }

    private List<String> applyLineReplacePatch(List<String> original, String patchText) {
        List<String> result = new ArrayList<>(original);
        String[] lines = patchText.strip().split("\n");
        for (String line : lines) {
            String[] parts = line.split(":", 2);
            if (parts.length != 2)
                throw new IllegalArgumentException("Invalid patch line: " + line);

            int lineNum = Integer.parseInt(parts[0].strip()) - 1;
            String newText = parts[1].strip();

            if (lineNum < 0 || lineNum >= result.size()) {
                throw new IndexOutOfBoundsException("Line number out of range: " + (lineNum + 1));
            }

            result.set(lineNum, newText);
        }
        return result;
    }
}
