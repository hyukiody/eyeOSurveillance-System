package backend.entities.gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

public class SwingImageClient {

    private final String API_URL = "http://localhost:8080/api/images/invert";
    private final HttpClient client;

    public SwingImageClient() {
        this.client = HttpClient.newHttpClient();
    }

    // Accepts a callback (Consumer) to update the UI when done
    public void sendImage(File imageFile, Consumer<byte[]> onSuccess, Consumer<String> onError) {
        String boundary = "---boundary-" + UUID.randomUUID().toString();
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(ofMimeMultipartData(imageFile, boundary))
                .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        SwingUtilities.invokeLater(() -> onSuccess.accept(response.body()));
                    } else {
                        SwingUtilities.invokeLater(() -> onError.accept("Server Error: " + response.statusCode()));
                    }
                })
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> onError.accept("Connection Error: " + ex.getMessage()));
                    return null;
                });

        } catch (Exception e) {
            onError.accept("Client Error: " + e.getMessage());
        }
    }

    // Helper to build Multipart Body manually for Java HttpClient
    private HttpRequest.BodyPublisher ofMimeMultipartData(File file, String boundary) throws IOException {
        List<byte[]> byteArrays = new ArrayList<>();
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) mimeType = "application/octet-stream";

        // 1. Header part
        String header = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n" +
                        "Content-Type: " + mimeType + "\r\n\r\n";
        byteArrays.add(header.getBytes(StandardCharsets.UTF_8));

        // 2. File content
        byteArrays.add(Files.readAllBytes(file.toPath()));

        // 3. Footer part
        String footer = "\r\n--" + boundary + "--\r\n";
        byteArrays.add(footer.getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}