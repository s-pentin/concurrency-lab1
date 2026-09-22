package org.labs;

import java.util.concurrent.CompletableFuture;

public class SoupRequest {
    private final int programmerId;
    private final CompletableFuture<SoupResult> result;

    public SoupRequest(int programmerId) {
        this.programmerId = programmerId;
        this.result = new CompletableFuture<>();
    }

    public int getProgrammerId() {
        return programmerId;
    }

    public CompletableFuture<SoupResult> getResult() {
        return result;
    }

    public void complete(SoupResult soupResult) {
        result.complete(soupResult);
    }
}
