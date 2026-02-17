package app.controller;

import app.http.RequestContext;
import app.http.Router;
import app.model.Patient;
import app.service.DeliveryService;
import app.service.ServiceException;

import java.io.IOException;
import java.util.UUID;

public class DeliveryController extends ControllerSupport {
    private final DeliveryService deliveryService;
    // Initializes delivery controller.
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // Registers routes.
    public void registerRoutes(Router router) {
        router.add("GET", "/delivery/confirm", this::handleConfirm);
        router.add("GET", "/delivery/respond", this::handleResponse);
    }

    private void handleConfirm(RequestContext ctx) throws IOException {
        String patientIdRaw = ctx.queryParam("patient");
        String token = ctx.queryParam("token");
        // Performs if.
        if (patientIdRaw.isBlank() || token.isBlank()) {
            ctx.html(400, layoutMessage("Invalid response link."));
            return;
        }
        UUID patientId;
        try {
            patientId = UUID.fromString(patientIdRaw);
        } catch (IllegalArgumentException e) {
            ctx.html(400, layoutMessage("Invalid response link."));
            return;
        }
        // Performs if.
        if (!deliveryService.isValidToken(patientId, token)) {
            ctx.html(403, layoutMessage("This response link is not valid."));
            return;
        }
        String yesLink = "/delivery/respond?patient=" + patientId + "&choice=yes&token=" + token;
        String noLink = "/delivery/respond?patient=" + patientId + "&choice=no&token=" + token;
        ctx.html(200, deliveryConfirmPage(yesLink, noLink));
    }

    private void handleResponse(RequestContext ctx) throws IOException {
        String patientIdRaw = ctx.queryParam("patient");
        String choice = ctx.queryParam("choice").toLowerCase();
        String token = ctx.queryParam("token");
        // Performs if.
        if (patientIdRaw.isBlank() || token.isBlank() || (!"yes".equals(choice) && !"no".equals(choice))) {
            ctx.html(400, layoutMessage("Invalid response link."));
            return;
        }
        UUID patientId;
        try {
            patientId = UUID.fromString(patientIdRaw);
        } catch (IllegalArgumentException e) {
            ctx.html(400, layoutMessage("Invalid response link."));
            return;
        }
        // Performs if.
        if (!deliveryService.isValidToken(patientId, token)) {
            ctx.html(403, layoutMessage("This response link is not valid."));
            return;
        }
        try {
            Patient patient = deliveryService.recordResponse(patientId, choice);
            String message = "Thanks! Your response was recorded as \"" + choice + "\".";
            ctx.html(200, layoutMessage(message));
        } catch (ServiceException ex) {
            ctx.html(ex.status(), layoutMessage(ex.getMessage()));
        }
    }

    // Performs layout message.
    private String layoutMessage(String message) {
        String safe = escapeHtml(message == null ? "" : message);
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\" />"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
                + "<title>Response</title>"
                + "<style>body{font-family:Arial,sans-serif;background:#f4f6f8;color:#12212f;margin:40px auto;max-width:640px;padding:0 16px;}"
                + ".card{background:#fff;padding:16px;border-radius:10px;box-shadow:0 4px 10px rgba(0,0,0,0.06);}</style>"
                + "</head><body><div class=\"card\"><p>" + safe + "</p></div></body></html>";
    }

    // Performs delivery confirm page.
    private String deliveryConfirmPage(String yesLink, String noLink) {
        String yesSafe = escapeHtml(yesLink);
        String noSafe = escapeHtml(noLink);
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\" />"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
                + "<title>Delivery Response</title>"
                + "<style>body{font-family:Arial,sans-serif;background:#f4f6f8;color:#12212f;margin:40px auto;max-width:640px;padding:0 16px;}"
                + ".card{background:#fff;padding:16px;border-radius:10px;box-shadow:0 4px 10px rgba(0,0,0,0.06);} "
                + ".actions{display:flex;gap:10px;margin-top:12px;} "
                + ".btn{display:inline-block;padding:10px 14px;border-radius:6px;text-decoration:none;color:#fff;} "
                + ".yes{background:#15803d;} .no{background:#b00020;}</style>"
                + "</head><body><div class=\"card\"><h3>Do want your medicine delivered?</h3>"
                + "<div class=\"actions\">"
                + "<a class=\"btn yes\" href=\"" + yesSafe + "\">Yes</a>"
                + "<a class=\"btn no\" href=\"" + noSafe + "\">No</a>"
                + "</div></div></body></html>";
    }

    // Performs escape html.
    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
