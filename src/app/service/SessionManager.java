package app.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, String> doctorSessions = new ConcurrentHashMap<>();
    private final Map<String, String> adminSessions = new ConcurrentHashMap<>();

    // Creates doctor session.
    public String createDoctorSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        doctorSessions.put(sessionId, username);
        return sessionId;
    }

    // Returns doctor username.
    public Optional<String> getDoctorUsername(String sessionId) {
        return Optional.ofNullable(doctorSessions.get(sessionId));
    }

    // Performs remove doctor session.
    public void removeDoctorSession(String sessionId) {
        doctorSessions.remove(sessionId);
    }

    // Creates admin session.
    public String createAdminSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        adminSessions.put(sessionId, username);
        return sessionId;
    }

    // Returns admin username.
    public Optional<String> getAdminUsername(String sessionId) {
        return Optional.ofNullable(adminSessions.get(sessionId));
    }

    // Performs remove admin session.
    public void removeAdminSession(String sessionId) {
        adminSessions.remove(sessionId);
    }
}
