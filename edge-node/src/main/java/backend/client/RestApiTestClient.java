package backend.client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple REST Client for testing Image Inverter API
 * Demonstrates how to call the REST endpoints programmatically
 */
public class RestApiTestClient {

    private static final String BASE_URL = "http://localhost:8080/api/images";
    private static final String BOUNDARY = "----FormBoundary" + System.nanoTime();

    public static void main(String[] args) throws Exception {
        System.out.println("=== Image Inverter REST API Test Client ===\n");

        // Test 1: Health Check
        System.out.println("1. Testing Health Check...");
        testHealthCheck();

        // Test 2: API Info
        System.out.println("\n2. Testing API Info...");
        testApiInfo();

        // Test 3: Invert Image (Binary)
        System.out.println("\n3. Testing Invert Image (Binary)...");
        testInvertImageBinary();

        // Test 4: Invert Image (JSON)
        System.out.println("\n4. Testing Invert Image (JSON)...");
        testInvertImageJson();

        System.out.println("\n=== Tests Complete ===");
    }

    private static void testHealthCheck() throws Exception {
        URL url = new URL(BASE_URL + "/health");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);

        System.out.println("Status: " + responseCode);
        System.out.println("Response: " + response);
        conn.disconnect();
    }

    private static void testApiInfo() throws Exception {
        URL url = new URL(BASE_URL + "/info");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);

        System.out.println("Status: " + responseCode);
        System.out.println("Response: " + response.substring(0, Math.min(500, response.length())) + "...");
        conn.disconnect();
    }

    private static void testInvertImageBinary() throws Exception {
        // Create a simple test image or use an existing one
        String testImagePath = "test-image.png";
        
        if (!Files.exists(Paths.get(testImagePath))) {
            System.out.println("Test image not found at: " + testImagePath);
            System.out.println("Skipping binary test...");
            return;
        }

        URL url = new URL(BASE_URL + "/invert");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

        // Send multipart form data
        try (OutputStream os = conn.getOutputStream()) {
            String fileContent = buildMultipartFileContent(testImagePath);
            os.write(fileContent.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Status: " + responseCode);
        System.out.println("Content-Type: " + conn.getContentType());
        System.out.println("Content-Length: " + conn.getContentLengthLong());

        if (responseCode == 200) {
            // Save the response image
            try (InputStream is = conn.getInputStream()) {
                Files.write(Paths.get("inverted-output.png"), is.readAllBytes());
                System.out.println("Inverted image saved to: inverted-output.png");
            }
        }

        conn.disconnect();
    }

    private static void testInvertImageJson() throws Exception {
        String testImagePath = "test-image.png";
        
        if (!Files.exists(Paths.get(testImagePath))) {
            System.out.println("Test image not found at: " + testImagePath);
            System.out.println("Skipping JSON test...");
            return;
        }

        URL url = new URL(BASE_URL + "/invert/json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        conn.setRequestProperty("Accept", "application/json");

        // Send multipart form data
        try (OutputStream os = conn.getOutputStream()) {
            String fileContent = buildMultipartFileContent(testImagePath);
            os.write(fileContent.getBytes());
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        String response = readResponse(conn);

        System.out.println("Status: " + responseCode);
        System.out.println("Response: " + response.substring(0, Math.min(500, response.length())) + "...");
        conn.disconnect();
    }

    private static String buildMultipartFileContent(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(BOUNDARY).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(Paths.get(filePath).getFileName()).append("\"\r\n");
        sb.append("Content-Type: application/octet-stream\r\n\r\n");
        
        // Note: This is a simplified version. For production, you'd need to handle binary data properly
        sb.append("--").append(BOUNDARY).append("--\r\n");
        
        return sb.toString();
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
