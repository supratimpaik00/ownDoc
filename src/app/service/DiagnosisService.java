package app.service;

import app.model.DiagnosisSession;
import app.repository.DiagnosisRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DiagnosisService {
    private final DiagnosisRepository diagnosisRepository;

    // Initializes diagnosis service.
    public DiagnosisService(DiagnosisRepository diagnosisRepository) {
        this.diagnosisRepository = diagnosisRepository;
    }

    // Saves session.
    public DiagnosisSession saveSession(UUID patientId, String diagnosis, String plan) {
        // Performs if.
        if (patientId == null) {
            // Performs service exception.
            throw new ServiceException(400, "Patient is required.");
        }
        // Performs if.
        if (isBlank(diagnosis) || isBlank(plan)) {
            // Performs service exception.
            throw new ServiceException(400, "Diagnosis and medication plan are required.");
        }
        DiagnosisSession session = new DiagnosisSession(UUID.randomUUID(), patientId, diagnosis.trim(), plan.trim(), LocalDateTime.now());
        diagnosisRepository.save(session);
        return session;
    }

    // Fetches by patient.
    public List<DiagnosisSession> listByPatient(UUID patientId) {
        // Performs if.
        if (patientId == null) {
            return List.of();
        }
        return diagnosisRepository.findByPatient(patientId);
    }

    // Checks whether blank.
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
