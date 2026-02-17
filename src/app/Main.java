package app;

import app.core.AppBootstrap;
import app.db.Database;
import app.db.InMemoryDatabase;
import app.db.JdbcDatabase;
import app.mail.ConsoleEmailService;
import app.mail.EmailService;
import app.http.Router;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        // Performs configure time zone.
        configureTimeZone();
        int port = resolvePort();
        Database database = resolveDatabase();
        EmailService emailService = new ConsoleEmailService();
        Router router = new AppBootstrap(database, emailService).router();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", router::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Server running on http://localhost:" + port);
    }

    // Resolves port.
    private static int resolvePort() {
        String env = System.getenv("PORT");
        // Performs if.
        if (env != null) {
            try {
                return Integer.parseInt(env);
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid PORT env var, falling back to 8080");
            }
        }
        return 8080;
    }

    // Resolves database.
    private static Database resolveDatabase() {
        String url = System.getenv("DB_URL");
        // Performs if.
        if (url == null || url.isBlank()) {
            System.out.println("DB_URL not set; using in-memory database.");
            // Performs in memory database.
            return new InMemoryDatabase();
        }
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASS");
        // Performs jdbc database.
        return new JdbcDatabase(url, user == null ? "" : user, password == null ? "" : password);
    }

    // Performs configure time zone.
    private static void configureTimeZone() {
        String tz = System.getenv("DB_TIMEZONE");
        // Performs if.
        if (tz == null || tz.isBlank()) {
            tz = "UTC";
        }
        // Performs if.
        if (!ZoneId.getAvailableZoneIds().contains(tz)) {
            System.out.println("Invalid DB_TIMEZONE '" + tz + "', falling back to UTC.");
            tz = "UTC";
        }
        System.setProperty("user.timezone", tz);
        TimeZone.setDefault(TimeZone.getTimeZone(tz));
    }
}
