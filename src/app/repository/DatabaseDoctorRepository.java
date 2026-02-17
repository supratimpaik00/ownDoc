package app.repository;

import app.db.Database;
import app.model.Doctor;

import java.util.List;
import java.util.Optional;

public class DatabaseDoctorRepository implements DoctorRepository {
    private final Database database;

    // Initializes database doctor repository.
    public DatabaseDoctorRepository(Database database) {
        this.database = database;
    }

    // Finds by username.
    @Override
    public Optional<Doctor> findByUsername(String username) {
        return database.getDoctor(username);
    }

    // Finds all.
    @Override
    public List<Doctor> findAll() {
        return database.getDoctors();
    }

    // Performs save.
    @Override
    public void save(Doctor doctor) {
        database.saveDoctor(doctor);
    }
}
