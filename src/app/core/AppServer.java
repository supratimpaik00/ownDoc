package app.core;

import app.db.Database;
import app.mail.EmailService;
import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;
import app.nlp.MedicationNlp;
import app.nlp.MedicationParseResult;
import app.util.PasswordHasher;
import app.view.HtmlTemplates;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AppServer {
    private final Database database;
    private final EmailService emailService;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> adminSessions = new ConcurrentHashMap<>();
    private final MedicationNlp medicationNlp = new MedicationNlp();
    private final String adminUser;
    private final String adminPass;
    private final String deliveryTokenSecret;

    public AppServer(Database database, EmailService emailService) {
        this.database = database;
        this.emailService = emailService;
        this.adminUser = resolveAdminUser();
        this.adminPass = resolveAdminPass();
        this.deliveryTokenSecret = resolveDeliveryTokenSecret();
    }

    public void handleRoot(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<Doctor> doctor = authenticate(exchange);
        if (doctor.isEmpty()) {
            redirect(exchange, "/login");
            return;
        }
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String search = query.getOrDefault("q", "").trim();
        String selected = query.getOrDefault("selected", "").trim();
        List<Patient> patients = database.getPatientsByDoctor(doctor.get().username());
        if (!search.isEmpty()) {
            String term = search.toLowerCase();
            patients = patients.stream()
                    .filter(p -> p.name().toLowerCase().contains(term)
                            || p.email().toLowerCase().contains(term)
                            || p.phone().toLowerCase().contains(term))
                    .toList();
        }
        Optional<Patient> selectedPatient = Optional.empty();
        if (!selected.isEmpty()) {
            try {
                UUID selId = UUID.fromString(selected);
                selectedPatient = patients.stream().filter(p -> p.id().equals(selId)).findFirst();
            } catch (IllegalArgumentException ignored) { }
        }
        if (selectedPatient.isEmpty() && !patients.isEmpty()) {
            selectedPatient = Optional.of(patients.get(0));
        }
        List<DiagnosisSession> history = selectedPatient.map(p -> database.getDiagnosisSessions(p.id())).orElse(List.of());
        String response = HtmlTemplates.dashboard(doctor.get(), patients, null, search, selectedPatient, history);
        writeResponse(exchange, 200, response);
    }

    public void handleSignup(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 200, HtmlTemplates.signup(null));
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String username = form.getOrDefault("username", "").trim();
        String name = form.getOrDefault("name", "").trim();
        String password = form.getOrDefault("password", "").trim();
        String qualifications = form.getOrDefault("qualifications", "").trim();
        if (username.isEmpty() || name.isEmpty() || password.isEmpty() || qualifications.isEmpty()) {
            writeResponse(exchange, 400, HtmlTemplates.signup("Username, name, password, and qualifications are required."));
            return;
        }
        if (database.getDoctor(username).isPresent()) {
            writeResponse(exchange, 400, HtmlTemplates.signup("Username already exists."));
            return;
        }
        Doctor doctor = new Doctor(username, name, PasswordHasher.hash(password), qualifications);
        database.saveDoctor(doctor);
        System.out.println("Doctor registered: " + name + " (" + username + ")");
        redirect(exchange, "/login");
    }

    public void handleLogin(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 200, HtmlTemplates.login(null));
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "").trim();
        Optional<Doctor> doctor = database.getDoctor(username);
        if (doctor.isEmpty() || !doctor.get().passwordHash().equals(PasswordHasher.hash(password))) {
            writeResponse(exchange, 401, HtmlTemplates.login("Invalid credentials."));
            return;
        }
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, username);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Set-Cookie", "SESSION=" + sessionId + "; Path=/; HttpOnly");
        redirect(exchange, "/");
    }

    public void handleLogout(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<String> sessionId = readSessionId(exchange);
        sessionId.ifPresent(sessions::remove);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Set-Cookie", "SESSION=; Path=/; Max-Age=0");
        redirect(exchange, "/login");
    }

    public void handlePatients(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<Doctor> doctor = authenticate(exchange);
        if (doctor.isEmpty()) {
            redirect(exchange, "/login");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String name = form.getOrDefault("name", "").trim();
        String email = form.getOrDefault("email", "").trim();
        String notes = form.getOrDefault("notes", "").trim();
        String address = form.getOrDefault("address", "").trim();
        String phone = form.getOrDefault("phone", "").trim();
        String ageRaw = form.getOrDefault("age", "").trim();
        String gender = form.getOrDefault("gender", "").trim();
        Integer age = parseAge(ageRaw);
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || age == null || gender.isEmpty()) {
            writeResponse(exchange, 400, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "Patient name, email, phone, address, age, and gender are required.", "", Optional.empty(), List.of()));
            return;
        }
        Patient patient = new Patient(UUID.randomUUID(), name, email, phone, age, gender, address, notes, doctor.get().username(), "pending");
        database.savePatient(patient);
        System.out.println("Patient created by " + doctor.get().name() + ": " + name + " (" + email + ")");
        redirect(exchange, "/?selected=" + patient.id());
    }

    public void handlePatientUpdate(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<Doctor> doctor = authenticate(exchange);
        if (doctor.isEmpty()) {
            redirect(exchange, "/login");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String patientIdRaw = form.getOrDefault("patientId", "").trim();
        String name = form.getOrDefault("name", "").trim();
        String email = form.getOrDefault("email", "").trim();
        String phone = form.getOrDefault("phone", "").trim();
        String notes = form.getOrDefault("notes", "").trim();
        String address = form.getOrDefault("address", "").trim();
        String ageRaw = form.getOrDefault("age", "").trim();
        String gender = form.getOrDefault("gender", "").trim();
        Integer age = parseAge(ageRaw);
        if (patientIdRaw.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || age == null || gender.isEmpty()) {
            writeResponse(exchange, 400, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "All fields are required to update patient.", "", Optional.empty(), List.of()));
            return;
        }
        UUID patientId = UUID.fromString(patientIdRaw);
        Optional<Patient> existing = database.getPatient(patientId);
        if (existing.isEmpty()) {
            writeResponse(exchange, 404, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "Patient not found.", "", Optional.empty(), List.of()));
            return;
        }
        if (!doctor.get().username().equals(existing.get().doctorUsername())) {
            writeResponse(exchange, 403, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "You cannot edit patients assigned to another doctor.", "", Optional.empty(), List.of()));
            return;
        }
        Patient updated = new Patient(patientId, name, email, phone, age, gender, address, notes, existing.get().doctorUsername(), existing.get().deliveryStatus());
        database.updatePatient(updated);
        redirect(exchange, "/?selected=" + patientId);
    }

    public void handlePatientDelete(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<Doctor> doctor = authenticate(exchange);
        if (doctor.isEmpty()) {
            redirect(exchange, "/login");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String patientIdRaw = form.getOrDefault("patientId", "").trim();
        if (patientIdRaw.isEmpty()) {
            writeResponse(exchange, 400, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "Patient id is required to delete.", "", Optional.empty(), List.of()));
            return;
        }
        UUID patientId = UUID.fromString(patientIdRaw);
        Optional<Patient> existing = database.getPatient(patientId);
        if (existing.isEmpty()) {
            writeResponse(exchange, 404, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "Patient not found.", "", Optional.empty(), List.of()));
            return;
        }
        if (!doctor.get().username().equals(existing.get().doctorUsername())) {
            writeResponse(exchange, 403, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "You cannot delete patients assigned to another doctor.", "", Optional.empty(), List.of()));
            return;
        }
        database.deletePatient(patientId);
        redirect(exchange, "/");
    }

    public void handleSessionSave(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<Doctor> doctor = authenticate(exchange);
        if (doctor.isEmpty()) {
            redirect(exchange, "/login");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String patientIdRaw = form.getOrDefault("patientId", "");
        String diagnosis = form.getOrDefault("diagnosis", "").trim();
        String medicationPlan = form.getOrDefault("medicationPlan", "").trim();
        String medication = form.getOrDefault("medication", "").trim();
        if (patientIdRaw.isEmpty() || diagnosis.isEmpty() || (medicationPlan.isEmpty() && medication.isEmpty())) {
            writeResponse(exchange, 400, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "Patient, diagnosis, and medication plan are required.", "", Optional.empty(), List.of()));
            return;
        }
        UUID patientId = UUID.fromString(patientIdRaw);
        Optional<Patient> patientOpt = database.getPatient(patientId);
        if (patientOpt.isEmpty()) {
            writeResponse(exchange, 404, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "Patient not found.", "", Optional.empty(), List.of()));
            return;
        }
        if (!doctor.get().username().equals(patientOpt.get().doctorUsername())) {
            writeResponse(exchange, 403, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "You cannot save diagnosis for another doctor's patient.", "", Optional.empty(), List.of()));
            return;
        }
        String plan = medicationPlan.isEmpty() ? medication : medicationPlan;
        database.saveDiagnosisSession(new DiagnosisSession(UUID.randomUUID(), patientId, diagnosis, plan, LocalDateTime.now()));
        System.out.println("Diagnosis session saved for patient " + patientId + " by " + doctor.get().name());
        redirect(exchange, "/?selected=" + patientId);
    }

    public void handlePrescriptions(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<Doctor> doctor = authenticate(exchange);
        if (doctor.isEmpty()) {
            redirect(exchange, "/login");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String patientIdRaw = form.getOrDefault("patientId", "");
        String diagnosis = form.getOrDefault("diagnosis", "").trim();
        String medicationPlan = form.getOrDefault("medicationPlan", "").trim();
        String medication = form.getOrDefault("medication", "").trim();
        if (patientIdRaw.isEmpty() || (medicationPlan.isEmpty() && medication.isEmpty())) {
            writeResponse(exchange, 400, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "Patient and medication plan are required.", "", Optional.empty(), List.of()));
            return;
        }
        UUID patientId = UUID.fromString(patientIdRaw);
        Optional<Patient> patientOpt = database.getPatient(patientId);
        if (patientOpt.isEmpty()) {
            writeResponse(exchange, 404, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "Patient not found.", "", Optional.empty(), List.of()));
            return;
        }
        if (!doctor.get().username().equals(patientOpt.get().doctorUsername())) {
            writeResponse(exchange, 403, HtmlTemplates.dashboard(doctor.get(), database.getPatientsByDoctor(doctor.get().username()), "You cannot send prescriptions for another doctor's patient.", "", Optional.empty(), List.of()));
            return;
        }
        Patient patient = patientOpt.get();
        String subject = "Prescription for " + patient.name();
        String plan = medicationPlan.isEmpty() ? medication : medicationPlan;
        String body = "Prescribed by: Dr. " + doctor.get().name() + " (" + doctor.get().qualifications() + ")\n"
                + "Diagnosis: " + (diagnosis.isEmpty() ? "N/A" : diagnosis)
                + "\nPlan:\n" + plan
                + "\nPrescribed at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        emailService.sendEmail(patient.email(), subject, body);
        System.out.println("Prescription sent to " + patient.email() + " plan:\n" + plan);
        database.saveDiagnosisSession(new DiagnosisSession(UUID.randomUUID(), patient.id(), diagnosis, plan, LocalDateTime.now()));
        redirect(exchange, "/?selected=" + patient.id());
    }

    public void handleMedicationNlp(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<Doctor> doctor = authenticate(exchange);
        if (doctor.isEmpty()) {
            writeJson(exchange, 401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, "{\"ok\":false,\"error\":\"method_not_allowed\"}");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String transcript = form.getOrDefault("transcript", "").trim();
        if (transcript.isEmpty()) {
            writeJson(exchange, 400, "{\"ok\":false,\"error\":\"empty_transcript\"}");
            return;
        }
        MedicationParseResult result = medicationNlp.parse(transcript);
        if (result.isEmpty()) {
            writeJson(exchange, 200, "{\"ok\":true,\"medication\":\"\",\"dosage\":\"\",\"days\":\"\"}");
            return;
        }
        String response = "{\"ok\":true,"
                + "\"medication\":\"" + jsonEscape(result.medication()) + "\","
                + "\"dosage\":\"" + jsonEscape(result.dosage()) + "\","
                + "\"days\":\"" + jsonEscape(result.days()) + "\"}";
        writeJson(exchange, 200, response);
    }

    public void handleAdminLogin(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            serveAdminIndex(exchange);
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "").trim();
        if (!adminUser.equals(username) || !adminPass.equals(password)) {
            writeResponse(exchange, 401, HtmlTemplates.adminLogin("Invalid credentials."));
            return;
        }
        String sessionId = UUID.randomUUID().toString();
        adminSessions.put(sessionId, username);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Set-Cookie", "ADMIN_SESSION=" + sessionId + "; Path=/; HttpOnly");
        redirect(exchange, "/admin");
    }

    public void handleAdminApiLogin(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, "{\"ok\":false,\"error\":\"method_not_allowed\"}");
            return;
        }
        Map<String, String> form = parseForm(exchange);
        String username = form.getOrDefault("username", "").trim();
        String password = form.getOrDefault("password", "").trim();
        if (!adminUser.equals(username) || !adminPass.equals(password)) {
            writeJson(exchange, 401, "{\"ok\":false,\"error\":\"invalid_credentials\"}");
            return;
        }
        String sessionId = UUID.randomUUID().toString();
        adminSessions.put(sessionId, username);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Set-Cookie", "ADMIN_SESSION=" + sessionId + "; Path=/; HttpOnly");
        writeJson(exchange, 200, "{\"ok\":true}");
    }

    public void handleAdminApiLogout(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, "{\"ok\":false,\"error\":\"method_not_allowed\"}");
            return;
        }
        Optional<String> sessionId = readAdminSessionId(exchange);
        sessionId.ifPresent(adminSessions::remove);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Set-Cookie", "ADMIN_SESSION=; Path=/; Max-Age=0");
        writeJson(exchange, 200, "{\"ok\":true}");
    }

    public void handleAdminAssets(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        Path root = adminUiRoot();
        Path target = root.resolve(path.substring(1)).normalize();
        if (!target.startsWith(root)) {
            writeResponse(exchange, 403, "Forbidden");
            return;
        }
        if (!Files.exists(target) || Files.isDirectory(target)) {
            writeResponse(exchange, 404, "Not Found");
            return;
        }
        byte[] bytes = Files.readAllBytes(target);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentTypeFor(target));
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public void handleAdminLogout(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<String> sessionId = readAdminSessionId(exchange);
        sessionId.ifPresent(adminSessions::remove);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Set-Cookie", "ADMIN_SESSION=; Path=/; Max-Age=0");
        redirect(exchange, "/admin/login");
    }

    public void handleAdminDashboard(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<String> admin = authenticateAdmin(exchange);
        if (admin.isEmpty()) {
            redirect(exchange, "/admin/login");
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        serveAdminIndex(exchange);
    }

    public void handleAdminDashboardData(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        Optional<String> admin = authenticateAdmin(exchange);
        if (admin.isEmpty()) {
            writeJson(exchange, 401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, "{\"ok\":false,\"error\":\"method_not_allowed\"}");
            return;
        }
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String selectedDoctor = query.getOrDefault("doctor", "").trim();
        List<Doctor> doctors = database.getDoctors();
        List<Patient> allPatients = database.getPatients();
        Map<String, Long> patientCounts = allPatients.stream()
                .collect(Collectors.groupingBy(Patient::doctorUsername, Collectors.counting()));
        int totalPatients = allPatients.size();
        long activeDoctors = patientCounts.values().stream().filter(count -> count != null && count > 0).count();
        Optional<Doctor> selected = Optional.empty();
        if (!selectedDoctor.isEmpty()) {
            selected = doctors.stream().filter(d -> d.username().equals(selectedDoctor)).findFirst();
        }
        if (selected.isEmpty() && !doctors.isEmpty()) {
            selected = Optional.of(doctors.get(0));
        }
        List<Patient> patients = selected.map(d -> database.getPatientsByDoctor(d.username())).orElse(List.of());
        String baseUrl = resolvePublicBaseUrl(exchange);
        String response = buildAdminDashboardJson(doctors, selected, patients, patientCounts, totalPatients, activeDoctors, baseUrl);
        writeJson(exchange, 200, response);
    }

    private String buildAdminDashboardJson(List<Doctor> doctors, Optional<Doctor> selected, List<Patient> patients,
                                           Map<String, Long> patientCounts, int totalPatients, long activeDoctors,
                                           String baseUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"summary\":{");
        sb.append("\"totalDoctors\":").append(doctors.size()).append(",");
        sb.append("\"activeDoctors\":").append(activeDoctors).append(",");
        sb.append("\"totalPatients\":").append(totalPatients);
        sb.append("},");
        sb.append("\"doctors\":[");
        for (int i = 0; i < doctors.size(); i++) {
            Doctor doctor = doctors.get(i);
            long doctorPatients = patientCounts.getOrDefault(doctor.username(), 0L);
            String status = doctorPatients > 0 ? "Active" : "Idle";
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{")
                    .append("\"username\":\"").append(jsonEscape(doctor.username())).append("\",")
                    .append("\"name\":\"").append(jsonEscape(doctor.name())).append("\",")
                    .append("\"qualifications\":\"").append(jsonEscape(doctor.qualifications())).append("\",")
                    .append("\"patients\":").append(doctorPatients).append(",")
                    .append("\"status\":\"").append(status).append("\"")
                    .append("}");
        }
        sb.append("],");
        sb.append("\"selectedDoctor\":");
        if (selected.isEmpty()) {
            sb.append("null");
        } else {
            Doctor doctor = selected.get();
            sb.append("{")
                    .append("\"username\":\"").append(jsonEscape(doctor.username())).append("\",")
                    .append("\"name\":\"").append(jsonEscape(doctor.name())).append("\",")
                    .append("\"qualifications\":\"").append(jsonEscape(doctor.qualifications())).append("\"")
                    .append("}");
        }
        sb.append(",");
        sb.append("\"patients\":[");
        for (int i = 0; i < patients.size(); i++) {
            Patient patient = patients.get(i);
            if (i > 0) {
                sb.append(",");
            }
            String deliveryMessage = buildDeliveryMessage(baseUrl, patient.id());
            sb.append("{")
                    .append("\"id\":\"").append(patient.id()).append("\",")
                    .append("\"name\":\"").append(jsonEscape(patient.name())).append("\",")
                    .append("\"email\":\"").append(jsonEscape(patient.email())).append("\",")
                    .append("\"phone\":\"").append(jsonEscape(patient.phone())).append("\",")
                    .append("\"age\":").append(patient.age() == null ? "null" : patient.age()).append(",")
                    .append("\"gender\":\"").append(jsonEscape(valueOrEmpty(patient.gender()))).append("\",")
                    .append("\"address\":\"").append(jsonEscape(valueOrEmpty(patient.address()))).append("\",")
                    .append("\"notes\":\"").append(jsonEscape(valueOrEmpty(patient.notes()))).append("\",")
                    .append("\"deliveryStatus\":\"").append(jsonEscape(valueOrEmpty(patient.deliveryStatus()))).append("\",")
                    .append("\"deliveryMessage\":\"").append(jsonEscape(deliveryMessage)).append("\",")
                    .append("\"history\":[");
            List<DiagnosisSession> history = database.getDiagnosisSessions(patient.id());
            for (int j = 0; j < history.size(); j++) {
                DiagnosisSession session = history.get(j);
                if (j > 0) {
                    sb.append(",");
                }
                sb.append("{")
                        .append("\"createdAt\":\"").append(jsonEscape(session.createdAt().toString())).append("\",")
                        .append("\"diagnosis\":\"").append(jsonEscape(session.diagnosis())).append("\",")
                        .append("\"plan\":\"").append(jsonEscape(session.plan())).append("\"")
                        .append("}");
            }
            sb.append("]}");
        }
        sb.append("]}");
        return sb.toString();
    }

    public void handleDeliveryConfirm(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String patientIdRaw = query.getOrDefault("patient", "").trim();
        String token = query.getOrDefault("token", "").trim();
        if (patientIdRaw.isEmpty() || token.isEmpty()) {
            writeResponse(exchange, 400, layoutMessage("Invalid response link."));
            return;
        }
        UUID patientId;
        try {
            patientId = UUID.fromString(patientIdRaw);
        } catch (IllegalArgumentException e) {
            writeResponse(exchange, 400, layoutMessage("Invalid response link."));
            return;
        }
        if (!isValidDeliveryToken(patientId, token)) {
            writeResponse(exchange, 403, layoutMessage("This response link is not valid."));
            return;
        }
        String yesLink = deliveryResponseLink(patientId, "yes", token);
        String noLink = deliveryResponseLink(patientId, "no", token);
        writeResponse(exchange, 200, deliveryConfirmPage(yesLink, noLink));
    }

    public void handleDeliveryResponse(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
        String patientIdRaw = query.getOrDefault("patient", "").trim();
        String choiceRaw = query.getOrDefault("choice", "").trim().toLowerCase();
        String token = query.getOrDefault("token", "").trim();
        if (patientIdRaw.isEmpty() || token.isEmpty() || (!"yes".equals(choiceRaw) && !"no".equals(choiceRaw))) {
            writeResponse(exchange, 400, layoutMessage("Invalid response link."));
            return;
        }
        UUID patientId;
        try {
            patientId = UUID.fromString(patientIdRaw);
        } catch (IllegalArgumentException e) {
            writeResponse(exchange, 400, layoutMessage("Invalid response link."));
            return;
        }
        if (!isValidDeliveryToken(patientId, token)) {
            writeResponse(exchange, 403, layoutMessage("This response link is not valid."));
            return;
        }
        Optional<Patient> patientOpt = database.getPatient(patientId);
        if (patientOpt.isEmpty()) {
            writeResponse(exchange, 404, layoutMessage("Patient not found."));
            return;
        }
        Patient patient = patientOpt.get();
        Patient updated = new Patient(
                patient.id(),
                patient.name(),
                patient.email(),
                patient.phone(),
                patient.age(),
                patient.gender(),
                patient.address(),
                patient.notes(),
                patient.doctorUsername(),
                choiceRaw
        );
        database.updatePatient(updated);
        String message = "Thanks! Your response was recorded as \"" + choiceRaw + "\".";
        writeResponse(exchange, 200, layoutMessage(message));
    }

    private Optional<Doctor> authenticate(HttpExchange exchange) {
        Optional<String> sessionId = readSessionId(exchange);
        if (sessionId.isEmpty()) {
            return Optional.empty();
        }
        String username = sessions.get(sessionId.get());
        if (username == null) {
            return Optional.empty();
        }
        return database.getDoctor(username);
    }

    private Optional<String> authenticateAdmin(HttpExchange exchange) {
        Optional<String> sessionId = readAdminSessionId(exchange);
        if (sessionId.isEmpty()) {
            return Optional.empty();
        }
        String username = adminSessions.get(sessionId.get());
        if (username == null) {
            return Optional.empty();
        }
        return Optional.of(username);
    }

    private Optional<String> readSessionId(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies == null) {
            return Optional.empty();
        }
        for (String cookieHeader : cookies) {
            String[] parts = cookieHeader.split(";\\s*");
            for (String part : parts) {
                if (part.startsWith("SESSION=")) {
                    return Optional.of(part.substring("SESSION=".length()));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> readAdminSessionId(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies == null) {
            return Optional.empty();
        }
        for (String cookieHeader : cookies) {
            String[] parts = cookieHeader.split(";\\s*");
            for (String part : parts) {
                if (part.startsWith("ADMIN_SESSION=")) {
                    return Optional.of(part.substring("ADMIN_SESSION=".length()));
                }
            }
        }
        return Optional.empty();
    }

    private Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            form.put(key, value);
        }
        return form;
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> query = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return query;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            query.put(key, value);
        }
        return query;
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private void writeResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void logRequest(HttpExchange exchange) {
        System.out.println("Request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
    }

    private void serveAdminIndex(HttpExchange exchange) throws IOException {
        Path index = adminUiRoot().resolve("index.html");
        if (!Files.exists(index)) {
            writeResponse(exchange, 200, "Admin UI not built. Run `npm run build` in frontend/.");
            return;
        }
        byte[] bytes = Files.readAllBytes(index);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Path adminUiRoot() {
        return Path.of("frontend", "dist");
    }

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

    private Integer parseAge(String raw) {
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

    private String resolvePublicBaseUrl(HttpExchange exchange) {
        String base = System.getenv("PUBLIC_BASE_URL");
        if (base != null && !base.isBlank()) {
            return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        }
        String host = exchange.getRequestHeaders().getFirst("Host");
        if (host == null || host.isBlank()) {
            return "http://localhost:8080";
        }
        String proto = exchange.getRequestHeaders().getFirst("X-Forwarded-Proto");
        if (proto == null || proto.isBlank()) {
            proto = "http";
        }
        return proto + "://" + host;
    }

    private String buildDeliveryMessage(String baseUrl, UUID patientId) {
        String token = createDeliveryToken(patientId);
        String link = baseUrl + "/delivery/confirm?patient=" + patientId + "&token=" + token;
        return "Do want ur medicine delivered? Tap this link: " + link;
    }

    private String deliveryResponseLink(UUID patientId, String choice, String token) {
        return "/delivery/respond?patient=" + patientId + "&choice=" + choice + "&token=" + token;
    }

    private String createDeliveryToken(UUID patientId) {
        String payload = patientId.toString();
        return hmacSha256Hex(payload, deliveryTokenSecret);
    }

    private boolean isValidDeliveryToken(UUID patientId, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String expected = createDeliveryToken(patientId);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] result = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate delivery token", e);
        }
    }

    private String resolveDeliveryTokenSecret() {
        String secret = System.getenv("DELIVERY_TOKEN_SECRET");
        if (secret == null || secret.isBlank()) {
            System.out.println("DELIVERY_TOKEN_SECRET not set; using a development default.");
            return "dev-secret";
        }
        return secret;
    }

    private String layoutMessage(String message) {
        String safe = escapeHtml(message == null ? "" : message);
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\" />"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />"
                + "<title>Response</title>"
                + "<style>body{font-family:Arial,sans-serif;background:#f4f6f8;color:#12212f;margin:40px auto;max-width:640px;padding:0 16px;}"
                + ".card{background:#fff;padding:16px;border-radius:10px;box-shadow:0 4px 10px rgba(0,0,0,0.06);}</style>"
                + "</head><body><div class=\"card\"><p>" + safe + "</p></div></body></html>";
    }

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

    private String escapeHtml(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String resolveAdminUser() {
        String user = System.getenv("ADMIN_USER");
        if (user == null || user.isBlank()) {
            System.out.println("ADMIN_USER not set; defaulting to admin.");
            return "admin";
        }
        return user;
    }

    private String resolveAdminPass() {
        String pass = System.getenv("ADMIN_PASS");
        if (pass == null || pass.isBlank()) {
            System.out.println("ADMIN_PASS not set; defaulting to adminpass.");
            return "adminpass";
        }
        return pass;
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
