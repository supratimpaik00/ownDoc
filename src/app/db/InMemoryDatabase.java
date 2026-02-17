package app.db;

import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;
import app.model.AdminUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDatabase implements Database {
    private final Map<String, Doctor> doctors = new ConcurrentHashMap<>();
    private final Map<UUID, Patient> patients = new ConcurrentHashMap<>();
    private final Map<UUID, List<DiagnosisSession>> sessionsByPatient = new ConcurrentHashMap<>();
    private final Map<String, AdminUser> adminUsers = new ConcurrentHashMap<>();

    // Returns doctor.
    public Optional<Doctor> getDoctor(String username) {
        return Optional.ofNullable(doctors.get(username));
    }

    // Saves doctor.
    public void saveDoctor(Doctor doctor) {
        doctors.put(doctor.username(), doctor);
    }

    // Saves patient.
    public void savePatient(Patient patient) {
        patients.put(patient.id(), patient);
    }

    // Updates patient.
    public void updatePatient(Patient patient) {
        patients.put(patient.id(), patient);
    }

    // Deletes patient.
    public void deletePatient(UUID id) {
        patients.remove(id);
        sessionsByPatient.remove(id);
    }

    // Returns patient.
    public Optional<Patient> getPatient(UUID id) {
        return Optional.ofNullable(patients.get(id));
    }

    // Returns patients.
    public List<Patient> getPatients() {
        List<Patient> list = new ArrayList<>(patients.values());
        list.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return Collections.unmodifiableList(list);
    }

    // Returns patients by doctor.
    @Override
    public List<Patient> getPatientsByDoctor(String username) {
        List<Patient> list = new ArrayList<>();
        // Performs for.
        for (Patient patient : patients.values()) {
            // Performs if.
            if (username.equals(patient.doctorUsername())) {
                list.add(patient);
            }
        }
        list.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return Collections.unmodifiableList(list);
    }

    // Returns doctors.
    @Override
    public List<Doctor> getDoctors() {
        List<Doctor> list = new ArrayList<>(doctors.values());
        list.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return Collections.unmodifiableList(list);
    }

    // Saves diagnosis session.
    public void saveDiagnosisSession(DiagnosisSession session) {
        sessionsByPatient.computeIfAbsent(session.patientId(), k -> Collections.synchronizedList(new ArrayList<>())).add(session);
    }

    // Returns diagnosis sessions.
    public List<DiagnosisSession> getDiagnosisSessions(UUID patientId) {
        List<DiagnosisSession> list = new ArrayList<>(sessionsByPatient.getOrDefault(patientId, List.of()));
        list.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
        return List.copyOf(list);
    }

    // Returns admin user.
    public Optional<AdminUser> getAdminUserByUsername(String username) {
        return Optional.ofNullable(adminUsers.get(username));
    }

    // Returns admin user by phone.
    public Optional<AdminUser> getAdminUserByPhone(String phone) {
        // Performs if.
        if (phone == null) {
            return Optional.empty();
        }
        return adminUsers.values().stream()
                .filter(user -> phone.equals(user.phone()))
                .findFirst();
    }

    // Saves admin user.
    public void saveAdminUser(AdminUser user) {
        adminUsers.put(user.username(), user);
    }

    // Updates admin password.
    public void updateAdminPassword(String username, String passwordHash) {
        AdminUser existing = adminUsers.get(username);
        // Performs if.
        if (existing == null) {
            return;
        }
        adminUsers.put(username, new AdminUser(existing.username(), passwordHash, existing.phone()));
    }
}
