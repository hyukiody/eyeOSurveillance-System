package com.teraapi.stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * HTTP Request/Response handler for stream processing
 */
public class StreamRequestHandler {

    private final JwtValidationUtil jwtValidator;
    private final LicenseValidationService licenseValidator;
    private final Gson gson;

    public StreamRequestHandler(String jwtSecret) {
        this.jwtValidator = new JwtValidationUtil(jwtSecret);
        this.licenseValidator = new LicenseValidationService();
        this.gson = new Gson();
    }

    /**
     * Process incoming HTTP request
     */
    public void handleRequest(String method, String path, String authHeader, InputStream requestBody, OutputStream responseBody) throws Exception {
        try {
            // Validate JWT token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(responseBody, 401, "Missing or invalid Authorization header");
                return;
            }

            String token = jwtValidator.extractBearerToken(authHeader);
            if (!jwtValidator.isTokenValid(token)) {
                sendError(responseBody, 401, "Invalid or expired token");
                return;
            }

            // Extract user information from token
            String username = jwtValidator.getUsernameFromToken(token);
            String role = jwtValidator.getRoleFromToken(token);

            // Route based on path
            if (path.equals("/api/stream/process")) {
                handleStreamProcessing(requestBody, responseBody, username, role);
            } else if (path.equals("/api/stream/encrypt")) {
                handleEncryption(requestBody, responseBody, username, role);
            } else if (path.equals("/api/stream/decrypt")) {
                handleDecryption(requestBody, responseBody, username, role);
            } else if (path.equals("/health")) {
                sendSuccess(responseBody, "{\"status\": \"UP\", \"service\": \"StreamProcessingService\"}");
            } else {
                sendError(responseBody, 404, "Endpoint not found");
            }

        } catch (Exception e) {
            sendError(responseBody, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handleStreamProcessing(InputStream requestBody, OutputStream responseBody, String username, String role) throws Exception {
        // Read request body
        byte[] buffer = new byte[8192];
        int bytesRead = requestBody.read(buffer);
        String requestData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

        JsonObject request = gson.fromJson(requestData, JsonObject.class);
        String data = request.get("data").getAsString();

        // Validate license
        long dataSize = data.getBytes().length;
        if (!licenseValidator.isValidLicense(role, username, dataSize)) {
            sendError(responseBody, 403, "Data exceeds tier limit");
            return;
        }

        // Process stream
        JsonObject response = new JsonObject();
        response.addProperty("processed", true);
        response.addProperty("username", username);
        response.addProperty("bytesProcessed", dataSize);
        response.addProperty("timestamp", System.currentTimeMillis());

        sendSuccess(responseBody, gson.toJson(response));
    }

    private void handleEncryption(InputStream requestBody, OutputStream responseBody, String username, String role) throws Exception {
        byte[] buffer = new byte[8192];
        int bytesRead = requestBody.read(buffer);
        String requestData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

        JsonObject request = gson.fromJson(requestData, JsonObject.class);
        String plaintext = request.get("data").getAsString();

        try {
            String key = DataProtectionService.generateKey();
            String encoded = DataProtectionService.encode(plaintext, key);

            JsonObject response = new JsonObject();
            response.addProperty("encrypted", encrypted);
            response.addProperty("key", key);
            response.addProperty("username", username);
            response.addProperty("timestamp", System.currentTimeMillis());

            sendSuccess(responseBody, gson.toJson(response));
        } catch (Exception e) {
            sendError(responseBody, 400, "Encryption failed: " + e.getMessage());
        }
    }

    private void handleDecryption(InputStream requestBody, OutputStream responseBody, String username, String role) throws Exception {
        byte[] buffer = new byte[8192];
        int bytesRead = requestBody.read(buffer);
        String requestData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

        JsonObject request = gson.fromJson(requestData, JsonObject.class);
        String encrypted = request.get("encrypted").getAsString();
        String key = request.get("key").getAsString();

        try {
            String decrypted = EncryptionService.decrypt(encrypted, key);

            JsonObject response = new JsonObject();
            response.addProperty("decrypted", decrypted);
            response.addProperty("username", username);
            response.addProperty("timestamp", System.currentTimeMillis());

            sendSuccess(responseBody, gson.toJson(response));
        } catch (Exception e) {
            sendError(responseBody, 400, "Decryption failed: " + e.getMessage());
        }
    }

    private void sendSuccess(OutputStream out, String body) throws Exception {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + body.getBytes().length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;
        out.write(response.getBytes());
        out.flush();
    }

    private void sendError(OutputStream out, int statusCode, String message) throws Exception {
        String statusMessage = getStatusMessage(statusCode);
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        error.addProperty("status", statusCode);
        String body = gson.toJson(error);

        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + body.getBytes().length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                body;
        out.write(response.getBytes());
        out.flush();
    }

    private String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}
