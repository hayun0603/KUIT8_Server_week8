package http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
    private final OutputStream outputStream;
    private final Map<String, String> headers = new LinkedHashMap<>();

    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void forward(String path) throws IOException {
        String url = path.startsWith("/") ? path.substring(1) : path;
        if (url.isEmpty()) {
            url = "index.html";
        }

        Path webapp = Paths.get("webapp").toAbsolutePath().normalize();
        Path file = webapp.resolve(url).normalize();
        if (!file.startsWith(webapp) || !Files.isRegularFile(file)) {
            response(HttpStatus.NOT_FOUND, "text/html;charset=utf-8",
                    "<h1>404 Not Found</h1>".getBytes(StandardCharsets.UTF_8), null);
            return;
        }

        byte[] body = Files.readAllBytes(file);
        String contentType = file.toString().endsWith(".css")
                ? "text/css"
                : "text/html;charset=utf-8";
        response(HttpStatus.OK, contentType, body, null);
    }

    public void redirect(String location) throws IOException {
        redirect(location, null);
    }

    public void redirect(String location, String setCookie) throws IOException {
        headers.clear();
        headers.put("Location", location);
        if (setCookie != null) {
            headers.put("Set-Cookie", setCookie);
        }
        response(HttpStatus.FOUND, null, new byte[0], setCookie);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    private void response(HttpStatus status, String contentType, byte[] body,
                          String setCookie) throws IOException {
        String headerText = "HTTP/1.1 " + status.getCode() + " " + status.getMessage() + "\r\n";
        outputStream.write(headerText.getBytes(StandardCharsets.ISO_8859_1));

        if (contentType != null) {
            outputStream.write(("Content-Type: " + contentType + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
        }
        outputStream.write(("Content-Length: " + body.length + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
        outputStream.write("Connection: close\r\n".getBytes(StandardCharsets.ISO_8859_1));

        for (Map.Entry<String, String> header : headers.entrySet()) {
            outputStream.write((header.getKey() + ": " + header.getValue() + "\r\n")
                    .getBytes(StandardCharsets.ISO_8859_1));
        }
        outputStream.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
        outputStream.write(body);
        outputStream.flush();
    }
}
