package app.controller;

import app.dto.AdminDashboardDto;
import app.http.RequestContext;
import app.http.Router;
import app.service.AdminAuthService;
import app.service.AdminService;
import app.service.AdminPasswordResetService;
import app.service.ServiceException;
import app.service.SessionManager;
import app.view.HtmlTemplates;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class AdminController extends ControllerSupport {
    private final AdminAuthService adminAuthService;
    private final AdminService adminService;
    private final AdminPasswordResetService adminPasswordResetService;
    private final SessionManager sessionManager;

    // Initializes admin controller.
    public AdminController(AdminAuthService adminAuthService, AdminService adminService, AdminPasswordResetService adminPasswordResetService, SessionManager sessionManager) {
        this.adminAuthService = adminAuthService;
        this.adminService = adminService;
        this.adminPasswordResetService = adminPasswordResetService;
        this.sessionManager = sessionManager;
    }

    // Registers routes.
    public void registerRoutes(Router router) {
        router.add("GET", "/admin/login", this::handleAdminLoginPage);
        router.add("POST", "/admin/login", this::handleAdminLoginForm);
        router.add("POST", "/admin/logout", this::handleAdminLogout);
        router.add("GET", "/admin", this::handleAdminDashboardPage);
        router.add("POST", "/api/admin/login", this::handleAdminApiLogin);
        router.add("POST", "/api/admin/logout", this::handleAdminApiLogout);
        router.add("GET", "/api/admin/dashboard", this::handleAdminApiDashboard);
        router.add("POST", "/api/admin/password/request", this::handleAdminPasswordRequest);
        router.add("POST", "/api/admin/password/verify", this::handleAdminPasswordVerify);
        router.add("POST", "/api/admin/password/reset", this::handleAdminPasswordReset);
    }

    private void handleAdminLoginPage(RequestContext ctx) throws IOException {
        // Performs serve admin index.
        serveAdminIndex(ctx);
    }

    private void handleAdminDashboardPage(RequestContext ctx) throws IOException {
        Optional<String> admin = authenticateAdmin(ctx);
        // Performs if.
        if (admin.isEmpty()) {
            ctx.redirect("/admin/login");
            return;
        }
        // Performs serve admin index.
        serveAdminIndex(ctx);
    }

    private void handleAdminLoginForm(RequestContext ctx) throws IOException {
        try {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            adminAuthService.validate(username, password);
            String sessionId = sessionManager.createAdminSession(username);
            ctx.setCookie("ADMIN_SESSION", sessionId, -1, true);
            ctx.redirect("/admin");
        } catch (ServiceException ex) {
            ctx.html(ex.status(), HtmlTemplates.adminLogin(ex.getMessage()));
        }
    }

    private void handleAdminLogout(RequestContext ctx) throws IOException {
        ctx.cookie("ADMIN_SESSION").ifPresent(sessionManager::removeAdminSession);
        ctx.clearCookie("ADMIN_SESSION");
        ctx.redirect("/admin/login");
    }

    private void handleAdminApiLogin(RequestContext ctx) throws IOException {
        try {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            adminAuthService.validate(username, password);
            String sessionId = sessionManager.createAdminSession(username);
            ctx.setCookie("ADMIN_SESSION", sessionId, -1, true);
            ctx.json(200, "{\"ok\":true}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    private void handleAdminApiLogout(RequestContext ctx) throws IOException {
        ctx.cookie("ADMIN_SESSION").ifPresent(sessionManager::removeAdminSession);
        ctx.clearCookie("ADMIN_SESSION");
        ctx.json(200, "{\"ok\":true}");
    }

    private void handleAdminApiDashboard(RequestContext ctx) throws IOException {
        Optional<String> admin = authenticateAdmin(ctx);
        // Performs if.
        if (admin.isEmpty()) {
            ctx.json(401, "{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }
        String selectedDoctor = ctx.queryParam("doctor");
        String baseUrl = resolvePublicBaseUrl(ctx);
        AdminDashboardDto dashboard = adminService.buildDashboard(selectedDoctor, baseUrl);
        ctx.json(200, dashboard.toJson());
    }

    private void handleAdminPasswordRequest(RequestContext ctx) throws IOException {
        try {
            String phone = ctx.formParam("phone");
            adminPasswordResetService.requestOtp(phone);
            ctx.json(200, "{\"ok\":true}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    private void handleAdminPasswordVerify(RequestContext ctx) throws IOException {
        try {
            String phone = ctx.formParam("phone");
            String otp = ctx.formParam("otp");
            String token = adminPasswordResetService.verifyOtp(phone, otp);
            ctx.json(200, "{\"ok\":true,\"token\":\"" + app.dto.JsonUtil.escape(token) + "\"}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    private void handleAdminPasswordReset(RequestContext ctx) throws IOException {
        try {
            String token = ctx.formParam("token");
            String newPassword = ctx.formParam("password");
            adminPasswordResetService.resetPassword(token, newPassword);
            ctx.json(200, "{\"ok\":true}");
        } catch (ServiceException ex) {
            // Performs write json error.
            writeJsonError(ctx, ex);
        }
    }

    // Authenticates admin.
    private Optional<String> authenticateAdmin(RequestContext ctx) {
        return ctx.cookie("ADMIN_SESSION").flatMap(sessionManager::getAdminUsername);
    }

    private void serveAdminIndex(RequestContext ctx) throws IOException {
        Path index = adminUiRoot().resolve("index.html");
        // Performs if.
        if (!Files.exists(index)) {
            ctx.html(200, "Admin UI not built. Run `npm run build` in frontend/.");
            return;
        }
        byte[] bytes = Files.readAllBytes(index);
        ctx.exchange().getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
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
}
