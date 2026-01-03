package com.teraapi.stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Simple REST client that calls identity-service introspection endpoint.
 *
 * This is intentionally lightweight (JDK HttpClient + Gson) to keep the
 * stream-processing-service dependency footprint small.
 */
public class IdentityIntrospectionClient {

    public record IntrospectionResult(boolean active, String username, String role, String deviceId) {
    }

    private final URI endpoint;
    private final HttpClient httpClient;
    private final Gson gson;

    public IdentityIntrospectionClient(String endpointUrl) {
        this.endpoint = URI.create(endpointUrl);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.gson = new Gson();
    }

    public IntrospectionResult introspect(String token) {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("token", token);

            HttpRequest httpRequest = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return new IntrospectionResult(false, null, null, null);
            }

            JsonObject body = gson.fromJson(response.body(), JsonObject.class);
            boolean active = body.has("active") && body.get("active").getAsBoolean();
            if (!active) {
                return new IntrospectionResult(false, null, null, null);
            }

            String username = body.has("username") ? body.get("username").getAsString() : null;
            String role = body.has("role") ? body.get("role").getAsString() : null;
            String deviceId = body.has("deviceId") ? body.get("deviceId").getAsString() : null;

            return new IntrospectionResult(true, username, role, deviceId);

        } catch (Exception ignored) {
            return new IntrospectionResult(false, null, null, null);
        }
    }
}
