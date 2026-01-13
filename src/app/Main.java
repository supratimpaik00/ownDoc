package app;

import app.core.AppServer;
import app.db.Database;
import app.db.InMemoryDatabase;
import app.db.JdbcDatabase;
import app.mail.ConsoleEmailService;
import app.mail.EmailService;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        configureTimeZone();
        int port = resolvePort();
        Database database = resolveDatabase();
        EmailService emailService = new ConsoleEmailService();
        AppServer app = new AppServer(database, emailService);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", app::handleRoot);
        server.createContext("/signup", app::handleSignup);
        server.createContext("/login", app::handleLogin);
        server.createContext("/logout", app::handleLogout);
        server.createContext("/patients", app::handlePatients);
        server.createContext("/patients/update", app::handlePatientUpdate);
        server.createContext("/patients/delete", app::handlePatientDelete);
        server.createContext("/sessions/save", app::handleSessionSave);
        server.createContext("/prescriptions", app::handlePrescriptions);
        server.createContext("/nlp/medication", app::handleMedicationNlp);
        server.createContext("/admin", app::handleAdminDashboard);
        server.createContext("/admin/login", app::handleAdminLogin);
        server.createContext("/admin/logout", app::handleAdminLogout);
        server.createContext("/api/admin/login", app::handleAdminApiLogin);
        server.createContext("/api/admin/logout", app::handleAdminApiLogout);
        server.createContext("/api/admin/dashboard", app::handleAdminDashboardData);
        server.createContext("/assets", app::handleAdminAssets);
        server.createContext("/delivery/confirm", app::handleDeliveryConfirm);
        server.createContext("/delivery/respond", app::handleDeliveryResponse);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Server running on http://localhost:" + port);
    }

    private static int resolvePort() {
        String env = System.getenv("PORT");
        if (env != null) {
            try {
                return Integer.parseInt(env);
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid PORT env var, falling back to 8080");
            }
        }
        return 8080;
    }

    private static Database resolveDatabase() {
        String url = System.getenv("DB_URL");
        if (url == null || url.isBlank()) {
            System.out.println("DB_URL not set; using in-memory database.");
            return new InMemoryDatabase();
        }
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASS");
        return new JdbcDatabase(url, user == null ? "" : user, password == null ? "" : password);
    }

    private static void configureTimeZone() {
        String tz = System.getenv("DB_TIMEZONE");
        if (tz == null || tz.isBlank()) {
            tz = "UTC";
        }
        if (!ZoneId.getAvailableZoneIds().contains(tz)) {
            System.out.println("Invalid DB_TIMEZONE '" + tz + "', falling back to UTC.");
            tz = "UTC";
        }
        System.setProperty("user.timezone", tz);
        TimeZone.setDefault(TimeZone.getTimeZone(tz));
    }
}
