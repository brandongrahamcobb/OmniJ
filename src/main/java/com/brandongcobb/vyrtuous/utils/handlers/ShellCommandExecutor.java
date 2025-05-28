package com.brandongcobb.vyrtuous.utils.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.brandongcobb.vyrtuous.utils.handlers.*;
import com.brandongcobb.vyrtuous.utils.inc.*;
import com.brandongcobb.metadata.*;
import java.util.Collections;

class ShellCommandExecutor {

    private final List<String> shellHistory = new ArrayList<>();
    private final AIManager aiManager = new AIManager();

    public CompletableFuture<String> startShellLoop(ResponseObject initialResponse) {
        shellHistory.clear();
        return runShellCommandLoop(initialResponse);
    }

    private CompletableFuture<String> runShellCommandLoop(ResponseObject responseObject) {
        ToolHandler toolHandler = new ToolHandler();

        return toolHandler.executeShellCommandAsync(responseObject)
            .thenCompose(execResult -> {
                shellHistory.add(execResult);

                return aiManager.completeLocalRequest(
                        execResult,
                        null,
                        ModelRegistry.GEMINI_RESPONSE_MODEL.asString(),
                        "completion"
                    )
                    .thenCompose(nextResponse ->
                        nextResponse.completeGetShellToolFinished()
                            .thenCompose(finished -> {
                                if (Boolean.TRUE.equals(finished)) {
                                    return CompletableFuture.completedFuture(execResult);
                                } else {
                                    return runShellCommandLoop(nextResponse);
                                }
                            })
                    );
            });
    }


    public List<String> getShellHistory() {
        return Collections.unmodifiableList(shellHistory);
    }
}

