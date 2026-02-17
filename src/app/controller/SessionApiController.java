package app.controller;

import app.dto.JsonUtil;
import app.http.RequestContext;
import app.http.Router;
import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;
import app.service.DiagnosisService;
import app.service.DoctorService;
import app.service.PatientService;
import app.service.PrescriptionService;
import app.service.ServiceException;
import app.service.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SessionApiController extends ControllerSupport {
    private final DiagnosisService diagnosisService;
    private final PrescriptionService prescriptionService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final SessionManager sessionManager;

    public SessionApiController(DiagnosisService diagnosisService, PrescriptionService prescriptionService,
                                PatientService patientService, DoctorService doctorService,
                                SessionManager sessionManager) {
        this.diagnosisService = diagnosisService;
        this.prescriptionService = prescriptionService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.sessionManager = sessionManager;
    }

    // Registers routes.
    public void registerRoutes(Router router) {
        router.add("POST", "/api/sessions", this::handleSaveSession);
        router.add("GET", "/api/patients/{id}/sessions", this::handleListSessions);
        router.add("POST", "/api/prescriptions", this::handlePrescription);
    }

    private void handleSaveSession(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        try {
            UUID patientId = UUID.fromString(ctx.formParam("patientId"));
            Patient patient = patientService.findById(patientId)
                    .orElseThrow(() -> new ServiceException(404, "Patient not found."));
            // Performs if.
            if (!doctor.get().username().equals(patient.doctorUsername())) {
                // Performs service exception.
                throw new ServiceException(403, "You cannot save diagnosis for another doctor's patient.");
            }
            String diagnosis = ctx.formParam("diagnosis");
            String plan = choosePlan(ctx.formParam("medicationPlan"), ctx.formParam("medication"));
            DiagnosisSession session = diagnosisService.saveSession(patientId, diagnosis, plan);
            ctx.json(200, "{\"ok\":true,\"session\":" + sessionJson(session) + "}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    private void handleListSessions(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        try {
            UUID patientId = UUID.fromString(ctx.pathParam("id"));
            Patient patient = patientService.findById(patientId)
                    .orElseThrow(() -> new ServiceException(404, "Patient not found."));
            // Performs if.
            if (!doctor.get().username().equals(patient.doctorUsername())) {
                // Performs service exception.
                throw new ServiceException(403, "You cannot view another doctor's patient.");
            }
            List<DiagnosisSession> sessions = diagnosisService.listByPatient(patientId);
            StringBuilder sb = new StringBuilder();
            sb.append("{\"ok\":true,\"sessions\":[");
            for (int i = 0; i < sessions.size(); i++) {
                // Performs if.
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(sessionJson(sessions.get(i)));
            }
            sb.append("]}");
            ctx.json(200, sb.toString());
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    private void handlePrescription(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        try {
            UUID patientId = UUID.fromString(ctx.formParam("patientId"));
            Patient patient = patientService.findById(patientId)
                    .orElseThrow(() -> new ServiceException(404, "Patient not found."));
            // Performs if.
            if (!doctor.get().username().equals(patient.doctorUsername())) {
                // Performs service exception.
                throw new ServiceException(403, "You cannot send prescriptions for another doctor's patient.");
            }
            String diagnosis = ctx.formParam("diagnosis");
            String plan = choosePlan(ctx.formParam("medicationPlan"), ctx.formParam("medication"));
            DiagnosisSession session = prescriptionService.sendPrescription(doctor.get(), patient, diagnosis, plan);
            ctx.json(200, "{\"ok\":true,\"session\":" + sessionJson(session) + "}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    // Authenticates doctor.
    private Optional<Doctor> authenticateDoctor(RequestContext ctx) {
        return ctx.cookie("SESSION")
                .flatMap(sessionManager::getDoctorUsername)
                .flatMap(doctorService::findByUsername);
    }

    // Performs choose plan.
    private String choosePlan(String medicationPlan, String medication) {
        // Performs if.
        if (medicationPlan != null && !medicationPlan.isBlank()) {
            return medicationPlan;
        }
        return medication == null ? "" : medication;
    }

    // Performs session json.
    private String sessionJson(DiagnosisSession session) {
        return "{\"id\":\"" + session.id() + "\""
                + ",\"patientId\":\"" + session.patientId() + "\""
                + ",\"diagnosis\":\"" + JsonUtil.escape(session.diagnosis()) + "\""
                + ",\"plan\":\"" + JsonUtil.escape(session.plan()) + "\""
                + ",\"createdAt\":\"" + JsonUtil.escape(session.createdAt().toString()) + "\"}";
    }
}
