package app.repository;

import app.model.DiagnosisSession;

import java.util.List;
import java.util.UUID;

public interface DiagnosisRepository {
    // Performs save.
    void save(DiagnosisSession session);

    // Finds by patient.
    List<DiagnosisSession> findByPatient(UUID patientId);
}
