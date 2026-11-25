package http;

import http.util.HttpRequestUtils;
import http.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpRequest {
    private final HttpMethod method;
    private final String path;
    private final String httpVersion;
    private final HttpHeader headers;
    private final String body;
    private final Map<String, String> parameters;

    private HttpRequest(HttpMethod method, String path, String httpVersion,
                        HttpHeader headers, String body, Map<String, String> parameters) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
        this.parameters = Map.copyOf(parameters);
    }

    public static HttpRequest from(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null) {
            throw new IOException("Request line is missing");
        }

        String[] startLine = requestLine.split(" ");
        if (startLine.length != 3) {
            throw new IllegalArgumentException("Invalid HTTP request line");
        }

        HttpMethod method = HttpMethod.from(startLine[0]);
        String requestTarget = startLine[1];
        String httpVersion = startLine[2];
        HttpHeader headers = new HttpHeader();

        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int separator = line.indexOf(':');
            if (separator < 0) {
                throw new IllegalArgumentException("Invalid HTTP header");
            }
            headers.add(line.substring(0, separator).trim(),
                    line.substring(separator + 1).trim());
        }
        if (line == null) {
            throw new IOException("Request headers ended before empty line");
        }

        int queryIndex = requestTarget.indexOf('?');
        String path = queryIndex >= 0 ? requestTarget.substring(0, queryIndex) : requestTarget;
        String queryString = queryIndex >= 0 ? requestTarget.substring(queryIndex + 1) : "";

        String body = "";
        if (method == HttpMethod.POST) {
            String contentLength = headers.get("Content-Length");
            int length = contentLength == null ? 0 : Integer.parseInt(contentLength.trim());
            body = IOUtils.readData(reader, length);
        }

        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.putAll(method == HttpMethod.GET
                ? HttpRequestUtils.parseQueryParameter(queryString)
                : HttpRequestUtils.parseQueryParameter(body));

        return new HttpRequest(method, path, httpVersion, headers, body, parameters);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public HttpHeader getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getBody() {
        return body;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public boolean isLogined() {
        String cookie = getHeader("Cookie");
        if (cookie == null) {
            return false;
        }
        for (String item : cookie.split(";")) {
            String[] pair = item.trim().split("=", 2);
            if (pair.length == 2 && pair[0].trim().equals("logined")
                    && pair[1].trim().equals("true")) {
                return true;
            }
        }
        return false;
    }
}
