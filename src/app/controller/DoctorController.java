package app.controller;

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
import app.view.HtmlTemplates;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DoctorController extends ControllerSupport {
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final DiagnosisService diagnosisService;
    private final PrescriptionService prescriptionService;
    private final SessionManager sessionManager;

    public DoctorController(DoctorService doctorService, PatientService patientService,
                            DiagnosisService diagnosisService, PrescriptionService prescriptionService,
                            SessionManager sessionManager) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.diagnosisService = diagnosisService;
        this.prescriptionService = prescriptionService;
        this.sessionManager = sessionManager;
    }

    // Registers routes.
    public void registerRoutes(Router router) {
        router.add("GET", "/", this::handleDashboard);
        router.add("GET", "/login", this::handleLoginPage);
        router.add("POST", "/login", this::handleLogin);
        router.add("POST", "/logout", this::handleLogout);
        router.add("GET", "/signup", this::handleSignupPage);
        router.add("POST", "/signup", this::handleSignup);
        router.add("POST", "/patients", this::handlePatients);
        router.add("POST", "/patients/update", this::handlePatientUpdate);
        router.add("POST", "/patients/delete", this::handlePatientDelete);
        router.add("POST", "/sessions/save", this::handleSessionSave);
        router.add("POST", "/prescriptions", this::handlePrescriptions);
    }

    private void handleDashboard(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.redirect("/login");
            return;
        }
        String search = ctx.queryParam("q").trim();
        String selected = ctx.queryParam("selected").trim();
        List<Patient> patients = patientService.listByDoctor(doctor.get().username());
        // Performs if.
        if (!search.isEmpty()) {
            String term = search.toLowerCase();
            patients = patients.stream()
                    .filter(p -> p.name().toLowerCase().contains(term)
                            || p.email().toLowerCase().contains(term)
                            || p.phone().toLowerCase().contains(term))
                    .toList();
        }
        Optional<Patient> selectedPatient = Optional.empty();
        // Performs if.
        if (!selected.isEmpty()) {
            try {
                UUID selId = UUID.fromString(selected);
                selectedPatient = patients.stream().filter(p -> p.id().equals(selId)).findFirst();
            } catch (IllegalArgumentException ignored) { }
        }
        // Performs if.
        if (selectedPatient.isEmpty() && !patients.isEmpty()) {
            selectedPatient = Optional.of(patients.get(0));
        }
        List<DiagnosisSession> history = selectedPatient.map(p -> diagnosisService.listByPatient(p.id())).orElse(List.of());
        ctx.html(200, HtmlTemplates.dashboard(doctor.get(), patients, null, search, selectedPatient, history));
    }

    private void handleLoginPage(RequestContext ctx) throws IOException {
        ctx.html(200, HtmlTemplates.login(null));
    }

    private void handleLogin(RequestContext ctx) throws IOException {
        try {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            Doctor doctor = doctorService.authenticate(username, password);
            String sessionId = sessionManager.createDoctorSession(doctor.username());
            ctx.setCookie("SESSION", sessionId, -1, true);
            ctx.redirect("/");
        } catch (ServiceException ex) {
            ctx.html(ex.status(), HtmlTemplates.login(ex.getMessage()));
        }
    }

    private void handleLogout(RequestContext ctx) throws IOException {
        ctx.cookie("SESSION").ifPresent(sessionManager::removeDoctorSession);
        ctx.clearCookie("SESSION");
        ctx.redirect("/login");
    }

    private void handleSignupPage(RequestContext ctx) throws IOException {
        ctx.html(200, HtmlTemplates.signup(null));
    }

    private void handleSignup(RequestContext ctx) throws IOException {
        try {
            doctorService.register(ctx.formParam("username"), ctx.formParam("name"),
                    ctx.formParam("password"), ctx.formParam("qualifications"));
            ctx.redirect("/login");
        } catch (ServiceException ex) {
            ctx.html(ex.status(), HtmlTemplates.signup(ex.getMessage()));
        }
    }

    private void handlePatients(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.redirect("/login");
            return;
        }
        try {
            Integer age = parseAge(ctx.formParam("age"));
            Patient patient = patientService.createPatient(doctor.get().username(),
                    ctx.formParam("name"),
                    ctx.formParam("email"),
                    ctx.formParam("phone"),
                    age,
                    ctx.formParam("gender"),
                    ctx.formParam("address"),
                    ctx.formParam("notes"));
            ctx.redirect("/?selected=" + patient.id());
        } catch (ServiceException ex) {
            List<Patient> patients = patientService.listByDoctor(doctor.get().username());
            ctx.html(ex.status(), HtmlTemplates.dashboard(doctor.get(), patients, ex.getMessage(), "", Optional.empty(), List.of()));
        }
    }

    private void handlePatientUpdate(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.redirect("/login");
            return;
        }
        try {
            String patientIdRaw = ctx.formParam("patientId");
            Integer age = parseAge(ctx.formParam("age"));
            UUID patientId = UUID.fromString(patientIdRaw);
            patientService.updatePatient(doctor.get().username(), patientId,
                    ctx.formParam("name"),
                    ctx.formParam("email"),
                    ctx.formParam("phone"),
                    age,
                    ctx.formParam("gender"),
                    ctx.formParam("address"),
                    ctx.formParam("notes"));
            ctx.redirect("/?selected=" + patientId);
        } catch (ServiceException ex) {
            List<Patient> patients = patientService.listByDoctor(doctor.get().username());
            ctx.html(ex.status(), HtmlTemplates.dashboard(doctor.get(), patients, ex.getMessage(), "", Optional.empty(), List.of()));
        }
    }

    private void handlePatientDelete(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.redirect("/login");
            return;
        }
        try {
            String patientIdRaw = ctx.formParam("patientId");
            UUID patientId = UUID.fromString(patientIdRaw);
            patientService.deletePatient(doctor.get().username(), patientId);
            ctx.redirect("/");
        } catch (ServiceException ex) {
            List<Patient> patients = patientService.listByDoctor(doctor.get().username());
            ctx.html(ex.status(), HtmlTemplates.dashboard(doctor.get(), patients, ex.getMessage(), "", Optional.empty(), List.of()));
        }
    }

    private void handleSessionSave(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.redirect("/login");
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
            String medicationPlan = ctx.formParam("medicationPlan");
            String medication = ctx.formParam("medication");
            String plan = medicationPlan.isBlank() ? medication : medicationPlan;
            diagnosisService.saveSession(patientId, diagnosis, plan);
            ctx.redirect("/?selected=" + patientId);
        } catch (ServiceException ex) {
            List<Patient> patients = patientService.listByDoctor(doctor.get().username());
            ctx.html(ex.status(), HtmlTemplates.dashboard(doctor.get(), patients, ex.getMessage(), "", Optional.empty(), List.of()));
        }
    }

    private void handlePrescriptions(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.redirect("/login");
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
            String medicationPlan = ctx.formParam("medicationPlan");
            String medication = ctx.formParam("medication");
            String plan = medicationPlan.isBlank() ? medication : medicationPlan;
            prescriptionService.sendPrescription(doctor.get(), patient, diagnosis, plan);
            ctx.redirect("/?selected=" + patientId);
        } catch (ServiceException ex) {
            List<Patient> patients = patientService.listByDoctor(doctor.get().username());
            ctx.html(ex.status(), HtmlTemplates.dashboard(doctor.get(), patients, ex.getMessage(), "", Optional.empty(), List.of()));
        }
    }

    // Authenticates doctor.
    private Optional<Doctor> authenticateDoctor(RequestContext ctx) {
        return ctx.cookie("SESSION")
                .flatMap(sessionManager::getDoctorUsername)
                .flatMap(doctorService::findByUsername);
    }
}
