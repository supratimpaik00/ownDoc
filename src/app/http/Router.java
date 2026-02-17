package app.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    private final List<Route> routes = new ArrayList<>();

    // Performs add.
    public Router add(String method, String pattern, RouteHandler handler) {
        routes.add(new Route(method.toUpperCase(), pattern, handler));
        return this;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();
        // Performs for.
        for (Route route : routes) {
            // Performs if.
            if (!route.method.equals(method)) {
                continue;
            }
            Map<String, String> params = route.match(path);
            // Performs if.
            if (params == null) {
                continue;
            }
            try {
                route.handler.handle(new RequestContext(exchange, params));
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                // Performs request context.
                new RequestContext(exchange, Map.of()).text(500, "Internal Server Error");
            }
            return;
        }
        // Performs request context.
        new RequestContext(exchange, Map.of()).text(404, "Not Found");
    }

    private static class Route {
        private final String method;
        private final String pattern;
        private final RouteHandler handler;
        private final String[] segments;

        // Performs route.
        private Route(String method, String pattern, RouteHandler handler) {
            this.method = method;
            this.pattern = normalize(pattern);
            this.handler = handler;
            this.segments = this.pattern.split("/");
        }

        // Performs match.
        private Map<String, String> match(String path) {
            String normalized = normalize(path);
            String[] parts = normalized.split("/");
            // Performs if.
            if (segments.length != parts.length) {
                return null;
            }
            Map<String, String> params = new HashMap<>();
            for (int i = 0; i < segments.length; i++) {
                String segment = segments[i];
                String part = parts[i];
                // Performs if.
                if (segment.startsWith("{") && segment.endsWith("}")) {
                    String key = segment.substring(1, segment.length() - 1);
                    params.put(key, part);
                } else if (!segment.equals(part)) {
                    return null;
                }
            }
            return params;
        }

        // Performs normalize.
        private String normalize(String value) {
            // Performs if.
            if (value == null || value.isBlank()) {
                return "/";
            }
            String trimmed = value.startsWith("/") ? value : "/" + value;
            // Performs if.
            if (trimmed.length() > 1 && trimmed.endsWith("/")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            return trimmed;
        }
    }
}
