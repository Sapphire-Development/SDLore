package dev.sapphire.sdlore.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class LoreApiClient {

    private final HttpClient httpClient;
    private final Gson gson;

    public LoreApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public CompletableFuture<LoreFetchResult> fetchLore(final String id) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://sapphi.dev/api/plugins/sdlore/" + id))
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseResponse)
                .exceptionally(throwable -> LoreFetchResult.failure("Failed to connect to the API: " + throwable.getMessage()));
    }

    private LoreFetchResult parseResponse(final HttpResponse<String> response) {
        final int statusCode = response.statusCode();
        final String body = response.body();

        if (body == null || body.isBlank()) {
            return LoreFetchResult.failure("The API returned an empty response (HTTP " + statusCode + ").");
        }

        final LoreResponse loreResponse;

        try {
            loreResponse = gson.fromJson(body, LoreResponse.class);
        } catch (final JsonSyntaxException exception) {
            return LoreFetchResult.failure("The API returned invalid JSON (HTTP " + statusCode + ").");
        }

        if (loreResponse == null) {
            return LoreFetchResult.failure("The API returned an unreadable response (HTTP " + statusCode + ").");
        }

        if (loreResponse.getError() != null && !loreResponse.getError().isBlank()) {
            return LoreFetchResult.failure(loreResponse.getError());
        }

        if (statusCode != 200) {
            return LoreFetchResult.failure("The API request failed (HTTP " + statusCode + ").");
        }

        if (loreResponse.getName() == null) {
            return LoreFetchResult.failure("The API response is missing a name.");
        }

        return LoreFetchResult.success(loreResponse);
    }
}
