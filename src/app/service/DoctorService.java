package app.service;

import app.model.Doctor;
import app.repository.DoctorRepository;
import app.util.PasswordHasher;

import java.util.List;
import java.util.Optional;

public class DoctorService {
    private final DoctorRepository doctorRepository;

    // Initializes doctor service.
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    // Performs register.
    public Doctor register(String username, String name, String password, String qualifications) {
        // Performs if.
        if (isBlank(username) || isBlank(name) || isBlank(password) || isBlank(qualifications)) {
            // Performs service exception.
            throw new ServiceException(400, "Username, name, password, and qualifications are required.");
        }
        // Performs if.
        if (doctorRepository.findByUsername(username).isPresent()) {
            // Performs service exception.
            throw new ServiceException(400, "Username already exists.");
        }
        Doctor doctor = new Doctor(username.trim(), name.trim(), PasswordHasher.hash(password.trim()), qualifications.trim());
        doctorRepository.save(doctor);
        return doctor;
    }

    // Performs authenticate.
    public Doctor authenticate(String username, String password) {
        // Performs if.
        if (isBlank(username) || isBlank(password)) {
            // Performs service exception.
            throw new ServiceException(401, "Invalid credentials.");
        }
        Optional<Doctor> doctor = doctorRepository.findByUsername(username.trim());
        // Performs if.
        if (doctor.isEmpty() || !doctor.get().passwordHash().equals(PasswordHasher.hash(password.trim()))) {
            // Performs service exception.
            throw new ServiceException(401, "Invalid credentials.");
        }
        return doctor.get();
    }

    // Finds by username.
    public Optional<Doctor> findByUsername(String username) {
        // Performs if.
        if (isBlank(username)) {
            return Optional.empty();
        }
        return doctorRepository.findByUsername(username.trim());
    }

    // Fetches doctors.
    public List<Doctor> listDoctors() {
        return doctorRepository.findAll();
    }

    // Checks whether blank.
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
