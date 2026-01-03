package com.teraapi.stream;

import com.teraapi.stream.util.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AdvancedStreamHandler implements HttpHandler {
    private final RateLimiter rateLimiter;
    private final DataProtection.AESGCMProtection aesGcm;
    private SecretKey aesKey;

    public AdvancedStreamHandler(int requestsPerMinute) throws Exception {
        this.rateLimiter = new RateLimiter(requestsPerMinute);
        this.aesKey = DataProtection.generateAESKey();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String clientId = exchange.getRemoteAddress().getHostName();

        RequestLogger.logRequest(method, path, clientId, startTime);

        // Rate limiting check
        if (!rateLimiter.allowRequest(clientId)) {
            sendResponse(exchange, 429, "{\"error\":\"Rate limit exceeded\"}");
            RequestLogger.logError(path, "Rate limit exceeded for " + clientId);
            return;
        }

        try {
            if ("POST".equals(method)) {
                if ("/api/stream/encrypt-gcm".equals(path)) {
                    handleGCMEncrypt(exchange);
                } else if ("/api/stream/decrypt-gcm".equals(path)) {
                    handleGCMDecrypt(exchange);
                } else if ("/api/stream/rsa-encrypt".equals(path)) {
                    handleRSAEncrypt(exchange);
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
                }
            } else if ("GET".equals(method)) {
                if ("/api/stream/rate-limit-status".equals(path)) {
                    handleRateLimitStatus(exchange, clientId);
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
                }
            }
            long duration = System.currentTimeMillis() - startTime;
            RequestLogger.logResponse(path, 200, duration);
        } catch (Exception e) {
            RequestLogger.logError(path, e.getMessage());
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleGCMEncrypt(HttpExchange exchange) throws Exception {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String plaintext = extractJson(body, "plaintext");

        String encoded = DataProtection.AESGCMProtection.encode(plaintext, aesKey);
        sendResponse(exchange, 200, "{\"encoded\":\"" + encoded + "\",\"algorithm\":\"AES-GCM-256\"}");
    }

    private void handleGCMDecrypt(HttpExchange exchange) throws Exception {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String encoded = extractJson(body, "encoded");

        String decoded = DataProtection.AESGCMProtection.decode(encoded, aesKey);
        sendResponse(exchange, 200, "{\"decoded\":\"" + decoded + "\"}");
    }

    private void handleRSAEncrypt(HttpExchange exchange) throws Exception {
        var keyPair = DataProtection.RSAProtection.generateKeyPair();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String plaintext = extractJson(body, "plaintext");

        String encoded = DataProtection.RSAProtection.encode(plaintext, keyPair.getPublic());
        sendResponse(exchange, 200, "{\"encoded\":\"" + encoded + "\",\"algorithm\":\"RSA-2048\"}");
    }

    private void handleRateLimitStatus(HttpExchange exchange, String clientId) throws IOException {
        int remaining = rateLimiter.getRemainingTokens(clientId);
        String response = "{\"remaining_requests\":" + remaining + ",\"limit_per_minute\":100}";
        sendResponse(exchange, 200, response);
    }

    private void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private String extractJson(String body, String key) {
        String pattern = "\"" + key + "\":\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(body);
        return m.find() ? m.group(1) : "";
    }
}
