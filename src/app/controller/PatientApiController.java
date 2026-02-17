package app.controller;

import app.dto.JsonUtil;
import app.http.RequestContext;
import app.http.Router;
import app.model.Patient;
import app.service.PatientService;
import app.service.ServiceException;
import app.service.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PatientApiController extends ControllerSupport {
    private final PatientService patientService;
    private final SessionManager sessionManager;

    // Initializes patient api controller.
    public PatientApiController(PatientService patientService, SessionManager sessionManager) {
        this.patientService = patientService;
        this.sessionManager = sessionManager;
    }

    // Registers routes.
    public void registerRoutes(Router router) {
        router.add("GET", "/api/patients", this::handleList);
        router.add("POST", "/api/patients", this::handleCreate);
        router.add("PUT", "/api/patients/{id}", this::handleUpdate);
        router.add("DELETE", "/api/patients/{id}", this::handleDelete);
    }

    private void handleList(RequestContext ctx) throws IOException {
        Optional<String> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        List<Patient> patients = patientService.listByDoctor(doctor.get());
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,\"patients\":[");
        for (int i = 0; i < patients.size(); i++) {
            // Performs if.
            if (i > 0) {
                sb.append(",");
            }
            sb.append(patientJson(patients.get(i)));
        }
        sb.append("]}");
        ctx.json(200, sb.toString());
    }

    private void handleCreate(RequestContext ctx) throws IOException {
        Optional<String> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        try {
            Integer age = parseAge(ctx.formParam("age"));
            Patient patient = patientService.createPatient(doctor.get(),
                    ctx.formParam("name"),
                    ctx.formParam("email"),
                    ctx.formParam("phone"),
                    age,
                    ctx.formParam("gender"),
                    ctx.formParam("address"),
                    ctx.formParam("notes"));
            ctx.json(201, "{\"ok\":true,\"patient\":" + patientJson(patient) + "}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    private void handleUpdate(RequestContext ctx) throws IOException {
        Optional<String> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        try {
            UUID patientId = UUID.fromString(ctx.pathParam("id"));
            Integer age = parseAge(ctx.formParam("age"));
            Patient patient = patientService.updatePatient(doctor.get(), patientId,
                    ctx.formParam("name"),
                    ctx.formParam("email"),
                    ctx.formParam("phone"),
                    age,
                    ctx.formParam("gender"),
                    ctx.formParam("address"),
                    ctx.formParam("notes"));
            ctx.json(200, "{\"ok\":true,\"patient\":" + patientJson(patient) + "}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    private void handleDelete(RequestContext ctx) throws IOException {
        Optional<String> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        try {
            UUID patientId = UUID.fromString(ctx.pathParam("id"));
            patientService.deletePatient(doctor.get(), patientId);
            ctx.json(200, "{\"ok\":true}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    // Authenticates doctor.
    private Optional<String> authenticateDoctor(RequestContext ctx) {
        return ctx.cookie("SESSION")
                .flatMap(sessionManager::getDoctorUsername);
    }

    // Performs patient json.
    private String patientJson(Patient patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(patient.id()).append("\"");
        sb.append(",\"name\":\"").append(JsonUtil.escape(patient.name())).append("\"");
        sb.append(",\"email\":\"").append(JsonUtil.escape(patient.email())).append("\"");
        sb.append(",\"phone\":\"").append(JsonUtil.escape(patient.phone())).append("\"");
        sb.append(",\"age\":").append(patient.age() == null ? "null" : patient.age());
        sb.append(",\"gender\":\"").append(JsonUtil.escape(valueOrEmpty(patient.gender()))).append("\"");
        sb.append(",\"address\":\"").append(JsonUtil.escape(valueOrEmpty(patient.address()))).append("\"");
        sb.append(",\"notes\":\"").append(JsonUtil.escape(valueOrEmpty(patient.notes()))).append("\"");
        sb.append(",\"deliveryStatus\":\"").append(JsonUtil.escape(valueOrEmpty(patient.deliveryStatus()))).append("\"");
        sb.append("}");
        return sb.toString();
    }

    // Performs value or empty.
    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
