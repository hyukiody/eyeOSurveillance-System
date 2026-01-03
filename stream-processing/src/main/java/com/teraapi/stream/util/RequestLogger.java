package com.teraapi.stream.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RequestLogger {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logRequest(String method, String path, String clientId, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(String.format(
            "[%s] %s %s | Client: %s | Duration: %dms",
            timestamp, method, path, clientId, duration
        ));
    }

    public static void logResponse(String path, int statusCode, long duration) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println(String.format(
            "[%s] Response: %s | Status: %d | Duration: %dms",
            timestamp, path, statusCode, duration
        ));
    }

    public static void logError(String path, String error) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.err.println(String.format(
            "[%s] ERROR: %s | Details: %s",
            timestamp, path, error
        ));
    }
}
