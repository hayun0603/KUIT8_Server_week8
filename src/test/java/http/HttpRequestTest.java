package http;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {
    @Test
    void parsesPostRequestAndFormBody() throws Exception {
        String body = "userId=student&password=pass&name=Sample&email=student%40example.com";
        String request = "POST /user/signup HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n"
                + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n"
                + body;

        HttpRequest parsed = HttpRequest.from(new BufferedReader(new StringReader(request)));

        assertEquals(HttpMethod.POST, parsed.getMethod());
        assertEquals("/user/signup", parsed.getPath());
        assertEquals("localhost:8080", parsed.getHeader("host"));
        assertEquals("student", parsed.getParameter("userId"));
        assertEquals("student@example.com", parsed.getParameter("email"));
    }

    @Test
    void parsesGetQueryParameters() throws Exception {
        String request = "GET /user/signup?userId=student&name=Sample+User HTTP/1.1\r\n"
                + "Host: localhost:8080\r\n\r\n";

        HttpRequest parsed = HttpRequest.from(new BufferedReader(new StringReader(request)));

        assertEquals(HttpMethod.GET, parsed.getMethod());
        assertEquals("/user/signup", parsed.getPath());
        assertEquals("student", parsed.getParameter("userId"));
        assertEquals("Sample User", parsed.getParameter("name"));
    }

    @Test
    void identifiesLoginedCookieExactly() throws Exception {
        String request = "GET /user/userList HTTP/1.1\r\n"
                + "Cookie: theme=dark; logined=true\r\n\r\n";

        HttpRequest parsed = HttpRequest.from(new BufferedReader(new StringReader(request)));
        assertTrue(parsed.isLogined());

        String notLogined = "GET /user/userList HTTP/1.1\r\n"
                + "Cookie: logined=false; theme=dark\r\n\r\n";
        assertFalse(HttpRequest.from(new BufferedReader(new StringReader(notLogined))).isLogined());
    }
}
