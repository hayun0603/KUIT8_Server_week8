package http;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS;

    public static HttpMethod from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("HTTP method must not be null");
        }
        return HttpMethod.valueOf(value.toUpperCase());
    }
}
