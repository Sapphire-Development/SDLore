package dev.sapphire.sdlore.api;

public final class LoreFetchResult {

    private final LoreResponse response;
    private final String errorMessage;

    private LoreFetchResult(final LoreResponse response, final String errorMessage) {
        this.response = response;
        this.errorMessage = errorMessage;
    }

    public static LoreFetchResult success(final LoreResponse response) {
        return new LoreFetchResult(response, null);
    }

    public static LoreFetchResult failure(final String errorMessage) {
        return new LoreFetchResult(null, errorMessage);
    }

    public boolean isSuccess() {
        return response != null;
    }

    public LoreResponse getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
