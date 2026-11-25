package http;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {
    @Test
    void forwardsHtmlWithByteLength() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpResponse response = new HttpResponse(output);

        response.forward("/index.html");

        String responseText = output.toString(StandardCharsets.ISO_8859_1);
        String[] parts = responseText.split("\\r\\n\\r\\n", 2);
        assertTrue(parts[0].contains("HTTP/1.1 200 OK"));
        assertTrue(parts[0].contains("Content-Type: text/html;charset=utf-8"));
        assertTrue(parts[0].contains("Content-Length: " + parts[1].getBytes(StandardCharsets.ISO_8859_1).length));
        assertFalse(parts[1].isEmpty());
    }

    @Test
    void forwardsCssWithCssContentType() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(output).forward("/css/styles.css");

        String responseText = output.toString(StandardCharsets.ISO_8859_1);
        assertTrue(responseText.contains("HTTP/1.1 200 OK"));
        assertTrue(responseText.contains("Content-Type: text/css"));
    }

    @Test
    void redirectsWithLocationAndCookie() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(output).redirect("/index.html", "logined=true; Path=/");

        String responseText = output.toString(StandardCharsets.ISO_8859_1);
        assertTrue(responseText.contains("HTTP/1.1 302 Found"));
        assertTrue(responseText.contains("Location: /index.html"));
        assertTrue(responseText.contains("Set-Cookie: logined=true; Path=/"));
        assertTrue(responseText.contains("Connection: close"));
        assertTrue(responseText.endsWith("\r\n\r\n"));
    }
}
