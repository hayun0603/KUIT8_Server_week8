package http;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class HttpHeader {
    private final Map<String, String> values = new LinkedHashMap<>();

    public void add(String name, String value) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Header name must not be blank");
        }
        values.put(name.toLowerCase(Locale.ROOT), value == null ? "" : value);
    }

    public String get(String name) {
        if (name == null) {
            return null;
        }
        return values.get(name.toLowerCase(Locale.ROOT));
    }

    public Map<String, String> asMap() {
        return Map.copyOf(values);
    }
}
