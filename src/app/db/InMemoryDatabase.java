package app.db;

import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;

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

    public Optional<Doctor> getDoctor(String username) {
        return Optional.ofNullable(doctors.get(username));
    }

    public void saveDoctor(Doctor doctor) {
        doctors.put(doctor.username(), doctor);
    }

    public void savePatient(Patient patient) {
        patients.put(patient.id(), patient);
    }

    public void updatePatient(Patient patient) {
        patients.put(patient.id(), patient);
    }

    public void deletePatient(UUID id) {
        patients.remove(id);
        sessionsByPatient.remove(id);
    }

    public Optional<Patient> getPatient(UUID id) {
        return Optional.ofNullable(patients.get(id));
    }

    public List<Patient> getPatients() {
        List<Patient> list = new ArrayList<>(patients.values());
        list.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<Patient> getPatientsByDoctor(String username) {
        List<Patient> list = new ArrayList<>();
        for (Patient patient : patients.values()) {
            if (username.equals(patient.doctorUsername())) {
                list.add(patient);
            }
        }
        list.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<Doctor> getDoctors() {
        List<Doctor> list = new ArrayList<>(doctors.values());
        list.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        return Collections.unmodifiableList(list);
    }

    public void saveDiagnosisSession(DiagnosisSession session) {
        sessionsByPatient.computeIfAbsent(session.patientId(), k -> Collections.synchronizedList(new ArrayList<>())).add(session);
    }

    public List<DiagnosisSession> getDiagnosisSessions(UUID patientId) {
        List<DiagnosisSession> list = new ArrayList<>(sessionsByPatient.getOrDefault(patientId, List.of()));
        list.sort((a, b) -> b.createdAt().compareTo(a.createdAt()));
        return List.copyOf(list);
    }
}
