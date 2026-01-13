package app.db;

import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Database {
    Optional<Doctor> getDoctor(String username);

    void saveDoctor(Doctor doctor);

    void savePatient(Patient patient);

    void updatePatient(Patient patient);

    void deletePatient(UUID id);

    Optional<Patient> getPatient(UUID id);

    List<Patient> getPatients();

    List<Patient> getPatientsByDoctor(String username);

    List<Doctor> getDoctors();

    void saveDiagnosisSession(DiagnosisSession session);

    List<DiagnosisSession> getDiagnosisSessions(UUID patientId);
}
