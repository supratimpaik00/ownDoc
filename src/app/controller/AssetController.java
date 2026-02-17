package app.controller;

import app.http.RequestContext;
import app.http.Router;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AssetController {
    // Registers routes.
    public void registerRoutes(Router router) {
        router.add("GET", "/assets/{file}", this::handleAsset);
    }

    private void handleAsset(RequestContext ctx) throws IOException {
        String file = ctx.pathParam("file");
        // Performs if.
        if (file == null || file.isBlank()) {
            ctx.text(404, "Not Found");
            return;
        }
        Path root = adminUiRoot().resolve("assets");
        Path target = root.resolve(file).normalize();
        // Performs if.
        if (!target.startsWith(root)) {
            ctx.text(403, "Forbidden");
            return;
        }
        // Performs if.
        if (!Files.exists(target) || Files.isDirectory(target)) {
            ctx.text(404, "Not Found");
            return;
        }
        byte[] bytes = Files.readAllBytes(target);
        ctx.exchange().getResponseHeaders().set("Content-Type", contentTypeFor(target));
        ctx.exchange().sendResponseHeaders(200, bytes.length);
        // Performs try.
        try (OutputStream os = ctx.exchange().getResponseBody()) {
            os.write(bytes);
        }
    }

    // Performs admin ui root.
    private Path adminUiRoot() {
        return Path.of("frontend", "dist");
    }

    // Performs content type for.
    private String contentTypeFor(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        return "application/octet-stream";
    }
}
