package app.repository;

import app.model.Patient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository {
    // Finds by id.
    Optional<Patient> findById(UUID id);

    // Finds all.
    List<Patient> findAll();

    // Finds by doctor.
    List<Patient> findByDoctor(String doctorUsername);

    // Performs save.
    void save(Patient patient);

    // Performs update.
    void update(Patient patient);

    // Performs delete.
    void delete(UUID id);
}
