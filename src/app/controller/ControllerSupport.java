package app.controller;

import app.service.ServiceException;
import app.http.RequestContext;

import java.io.IOException;
import java.time.ZoneId;
import java.util.TimeZone;

public class ControllerSupport {
    // Parses age.
    protected Integer parseAge(String raw) {
        // Performs if.
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int age = Integer.parseInt(raw);
            return age >= 0 ? age : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void writeJsonError(RequestContext ctx, ServiceException ex) throws IOException {
        ctx.json(ex.status(), "{\"ok\":false,\"error\":\"" + ex.getMessage() + "\"}");
    }

    // Resolves public base url.
    protected String resolvePublicBaseUrl(RequestContext ctx) {
        String base = System.getenv("PUBLIC_BASE_URL");
        // Performs if.
        if (base != null && !base.isBlank()) {
            return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        }
        String host = ctx.exchange().getRequestHeaders().getFirst("Host");
        // Performs if.
        if (host == null || host.isBlank()) {
            return "http://localhost:8080";
        }
        String proto = ctx.exchange().getRequestHeaders().getFirst("X-Forwarded-Proto");
        // Performs if.
        if (proto == null || proto.isBlank()) {
            proto = "http";
        }
        return proto + "://" + host;
    }

    // Performs configure time zone.
    protected void configureTimeZone() {
        String tz = System.getenv("DB_TIMEZONE");
        // Performs if.
        if (tz == null || tz.isBlank()) {
            tz = "UTC";
        }
        // Performs if.
        if (!ZoneId.getAvailableZoneIds().contains(tz)) {
            System.out.println("Invalid DB_TIMEZONE '" + tz + "', falling back to UTC.");
            tz = "UTC";
        }
        System.setProperty("user.timezone", tz);
        TimeZone.setDefault(TimeZone.getTimeZone(tz));
    }
}
