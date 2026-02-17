package app.repository;

import app.model.Doctor;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository {
    // Finds by username.
    Optional<Doctor> findByUsername(String username);

    // Finds all.
    List<Doctor> findAll();

    // Performs save.
    void save(Doctor doctor);
}
