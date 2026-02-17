package app.controller;

import app.dto.JsonUtil;
import app.http.RequestContext;
import app.http.Router;
import app.model.Doctor;
import app.service.DoctorService;
import app.service.ServiceException;
import app.service.SessionManager;

import java.io.IOException;
import java.util.Optional;

public class DoctorApiController extends ControllerSupport {
    private final DoctorService doctorService;
    private final SessionManager sessionManager;

    // Initializes doctor api controller.
    public DoctorApiController(DoctorService doctorService, SessionManager sessionManager) {
        this.doctorService = doctorService;
        this.sessionManager = sessionManager;
    }

    // Registers routes.
    public void registerRoutes(Router router) {
        router.add("POST", "/api/doctor/login", this::handleLogin);
        router.add("POST", "/api/doctor/logout", this::handleLogout);
        router.add("GET", "/api/doctor/me", this::handleMe);
    }

    private void handleLogin(RequestContext ctx) throws IOException {
        try {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            Doctor doctor = doctorService.authenticate(username, password);
            String sessionId = sessionManager.createDoctorSession(doctor.username());
            ctx.setCookie("SESSION", sessionId, -1, true);
            ctx.json(200, "{\"ok\":true}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    private void handleLogout(RequestContext ctx) throws IOException {
        ctx.cookie("SESSION").ifPresent(sessionManager::removeDoctorSession);
        ctx.clearCookie("SESSION");
        ctx.json(200, "{\"ok\":true}");
    }

    private void handleMe(RequestContext ctx) throws IOException {
        Optional<Doctor> doctor = authenticateDoctor(ctx);
        // Performs if.
        if (doctor.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        Doctor doc = doctor.get();
        ctx.json(200, "{\"ok\":true,"
                + "\"username\":\"" + JsonUtil.escape(doc.username()) + "\","
                + "\"name\":\"" + JsonUtil.escape(doc.name()) + "\","
                + "\"qualifications\":\"" + JsonUtil.escape(doc.qualifications()) + "\"}");
    }

    // Authenticates doctor.
    private Optional<Doctor> authenticateDoctor(RequestContext ctx) {
        return ctx.cookie("SESSION")
                .flatMap(sessionManager::getDoctorUsername)
                .flatMap(doctorService::findByUsername);
    }
}
