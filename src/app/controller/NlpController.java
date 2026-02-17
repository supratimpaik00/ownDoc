package app.controller;

import app.http.RequestContext;
import app.http.Router;
import app.nlp.MedicationParseResult;
import app.service.DoctorService;
import app.service.NlpService;
import app.service.ServiceException;
import app.service.SessionManager;

import java.io.IOException;
import java.util.Optional;

public class NlpController extends ControllerSupport {
    private final NlpService nlpService;
    private final DoctorService doctorService;
    private final SessionManager sessionManager;

    // Initializes nlp controller.
    public NlpController(NlpService nlpService, DoctorService doctorService, SessionManager sessionManager) {
        this.nlpService = nlpService;
        this.doctorService = doctorService;
        this.sessionManager = sessionManager;
    }

    // Registers routes.
    public void registerRoutes(Router router) {
        router.add("POST", "/nlp/medication", this::handleMedication);
        router.add("POST", "/api/nlp/medication", this::handleMedication);
    }

    private void handleMedication(RequestContext ctx) throws IOException {
        Optional<String> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        try {
            String transcript = ctx.formParam("transcript");
            MedicationParseResult result = nlpService.parseMedication(transcript);
            String response = "{\"ok\":true,"
                    + "\"medication\":\"" + escape(result.medication()) + "\","
                    + "\"dosage\":\"" + escape(result.dosage()) + "\","
                    + "\"days\":\"" + escape(result.days()) + "\"}";
            ctx.json(200, response);
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    // Authenticates doctor.
    private Optional<String> authenticateDoctor(RequestContext ctx) {
        return ctx.cookie("SESSION")
                .flatMap(sessionManager::getDoctorUsername)
                .flatMap(username -> doctorService.findByUsername(username).map(d -> username));
    }

    // Performs escape.
    private String escape(String value) {
        // Performs if.
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
