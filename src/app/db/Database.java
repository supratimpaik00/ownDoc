package app.db;

import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;
import app.model.AdminUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Database {
    // Returns doctor.
    Optional<Doctor> getDoctor(String username);

    // Saves doctor.
    void saveDoctor(Doctor doctor);

    // Saves patient.
    void savePatient(Patient patient);

    // Updates patient.
    void updatePatient(Patient patient);

    // Deletes patient.
    void deletePatient(UUID id);

    // Returns patient.
    Optional<Patient> getPatient(UUID id);

    // Returns patients.
    List<Patient> getPatients();

    // Returns patients by doctor.
    List<Patient> getPatientsByDoctor(String username);

    // Returns doctors.
    List<Doctor> getDoctors();

    // Saves diagnosis session.
    void saveDiagnosisSession(DiagnosisSession session);

    // Returns diagnosis sessions.
    List<DiagnosisSession> getDiagnosisSessions(UUID patientId);

    // Returns admin user.
    Optional<AdminUser> getAdminUserByUsername(String username);

    // Returns admin user by phone.
    Optional<AdminUser> getAdminUserByPhone(String phone);

    // Saves admin user.
    void saveAdminUser(AdminUser user);

    // Updates admin password.
    void updateAdminPassword(String username, String passwordHash);
}
