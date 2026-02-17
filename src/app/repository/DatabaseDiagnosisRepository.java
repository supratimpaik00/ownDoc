package app.repository;

import app.db.Database;
import app.model.DiagnosisSession;

import java.util.List;
import java.util.UUID;

public class DatabaseDiagnosisRepository implements DiagnosisRepository {
    private final Database database;

    // Initializes database diagnosis repository.
    public DatabaseDiagnosisRepository(Database database) {
        this.database = database;
    }

    // Performs save.
    @Override
    public void save(DiagnosisSession session) {
        database.saveDiagnosisSession(session);
    }

    // Finds by patient.
    @Override
    public List<DiagnosisSession> findByPatient(UUID patientId) {
        return database.getDiagnosisSessions(patientId);
    }
}
