package app.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestContext {
    private final HttpExchange exchange;
    private final Map<String, String> pathParams;
    private Map<String, String> queryParams;
    private Map<String, String> formParams;
    private String body;

    // Initializes request context.
    RequestContext(HttpExchange exchange, Map<String, String> pathParams) {
        this.exchange = exchange;
        this.pathParams = pathParams == null ? Map.of() : pathParams;
    }

    // Performs exchange.
    public HttpExchange exchange() {
        return exchange;
    }

    // Performs method.
    public String method() {
        return exchange.getRequestMethod();
    }

    // Performs path.
    public String path() {
        return exchange.getRequestURI().getPath();
    }

    // Performs path param.
    public String pathParam(String name) {
        return pathParams.get(name);
    }

    // Performs query params.
    public Map<String, String> queryParams() {
        // Performs if.
        if (queryParams == null) {
            queryParams = parseQuery(exchange.getRequestURI().getRawQuery());
        }
        return queryParams;
    }

    // Performs query param.
    public String queryParam(String name) {
        // Performs query params.
        return queryParams().getOrDefault(name, "");
    }

    public Map<String, String> formParams() throws IOException {
        // Performs if.
        if (formParams == null) {
            String contentType = Optional.ofNullable(exchange.getRequestHeaders().getFirst("Content-Type"))
                    .orElse("");
            // Performs if.
            if (!contentType.startsWith("application/x-www-form-urlencoded")) {
                formParams = Collections.emptyMap();
            } else {
                formParams = parseQuery(readBody());
            }
        }
        return formParams;
    }

    public String formParam(String name) throws IOException {
        // Performs form params.
        return formParams().getOrDefault(name, "");
    }

    public String body() throws IOException {
        // Performs read body.
        return readBody();
    }

    // Performs cookie.
    public Optional<String> cookie(String name) {
        String header = exchange.getRequestHeaders().getFirst("Cookie");
        // Performs if.
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        String[] parts = header.split(";");
        // Performs for.
        for (String part : parts) {
            String trimmed = part.trim();
            int idx = trimmed.indexOf('=');
            // Performs if.
            if (idx <= 0) {
                continue;
            }
            String key = trimmed.substring(0, idx);
            // Performs if.
            if (key.equals(name)) {
                return Optional.of(trimmed.substring(idx + 1));
            }
        }
        return Optional.empty();
    }

    // Updates cookie.
    public void setCookie(String name, String value, int maxAgeSeconds, boolean httpOnly) {
        StringBuilder cookie = new StringBuilder();
        cookie.append(name).append("=").append(value).append("; Path=/");
        // Performs if.
        if (maxAgeSeconds >= 0) {
            cookie.append("; Max-Age=").append(maxAgeSeconds);
        }
        // Performs if.
        if (httpOnly) {
            cookie.append("; HttpOnly");
        }
        exchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
    }

    // Performs clear cookie.
    public void clearCookie(String name) {
        // Updates cookie.
        setCookie(name, "", 0, true);
    }

    public void json(int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        // Performs try.
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public void html(int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        // Performs try.
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public void text(int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        // Performs try.
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public void redirect(String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private String readBody() throws IOException {
        // Performs if.
        if (body != null) {
            return body;
        }
        // Performs try.
        try (InputStream input = exchange.getRequestBody()) {
            body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        return body;
    }

    // Parses query.
    private Map<String, String> parseQuery(String raw) {
        // Performs if.
        if (raw == null || raw.isBlank()) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>();
        String[] pairs = raw.split("&");
        // Performs for.
        for (String pair : pairs) {
            // Performs if.
            if (pair.isEmpty()) {
                continue;
            }
            int idx = pair.indexOf('=');
            String key = idx > 0 ? pair.substring(0, idx) : pair;
            String value = idx > 0 ? pair.substring(idx + 1) : "";
            map.put(urlDecode(key), urlDecode(value));
        }
        return map;
    }

    // Performs url decode.
    private String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return value;
        }
    }
}
