package app.repository;

import app.db.Database;
import app.model.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DatabasePatientRepository implements PatientRepository {
    private final Database database;

    // Initializes database patient repository.
    public DatabasePatientRepository(Database database) {
        this.database = database;
    }

    // Finds by id.
    @Override
    public Optional<Patient> findById(UUID id) {
        return database.getPatient(id);
    }

    // Finds all.
    @Override
    public List<Patient> findAll() {
        return database.getPatients();
    }

    // Finds by doctor.
    @Override
    public List<Patient> findByDoctor(String doctorUsername) {
        return database.getPatientsByDoctor(doctorUsername);
    }

    // Performs save.
    @Override
    public void save(Patient patient) {
        database.savePatient(patient);
    }

    // Performs update.
    @Override
    public void update(Patient patient) {
        database.updatePatient(patient);
    }

    // Performs delete.
    @Override
    public void delete(UUID id) {
        database.deletePatient(id);
    }
}
