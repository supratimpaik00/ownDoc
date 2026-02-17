package app.service;

import app.model.Patient;
import app.repository.PatientRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PatientService {
    private final PatientRepository patientRepository;

    // Initializes patient service.
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient createPatient(String doctorUsername, String name, String email, String phone,
                                 Integer age, String gender, String address, String notes) {
        // Performs if.
        if (isBlank(doctorUsername)) {
            // Performs service exception.
            throw new ServiceException(401, "Unauthorized.");
        }
        // Performs validate patient fields.
        validatePatientFields(name, email, phone, age, gender, address);
        Patient patient = new Patient(UUID.randomUUID(), name.trim(), email.trim(), phone.trim(), age,
                gender.trim(), address.trim(), safeNotes(notes), doctorUsername, "pending");
        patientRepository.save(patient);
        return patient;
    }

    public Patient updatePatient(String doctorUsername, UUID patientId, String name, String email, String phone,
                                 Integer age, String gender, String address, String notes) {
        // Performs if.
        if (isBlank(doctorUsername)) {
            // Performs service exception.
            throw new ServiceException(401, "Unauthorized.");
        }
        // Performs validate patient fields.
        validatePatientFields(name, email, phone, age, gender, address);
        Patient existing = patientRepository.findById(patientId)
                .orElseThrow(() -> new ServiceException(404, "Patient not found."));
        // Performs if.
        if (!doctorUsername.equals(existing.doctorUsername())) {
            // Performs service exception.
            throw new ServiceException(403, "You cannot edit patients assigned to another doctor.");
        }
        Patient updated = new Patient(patientId, name.trim(), email.trim(), phone.trim(), age,
                gender.trim(), address.trim(), safeNotes(notes), existing.doctorUsername(), existing.deliveryStatus());
        patientRepository.update(updated);
        return updated;
    }

    // Deletes patient.
    public void deletePatient(String doctorUsername, UUID patientId) {
        // Performs if.
        if (isBlank(doctorUsername)) {
            // Performs service exception.
            throw new ServiceException(401, "Unauthorized.");
        }
        Patient existing = patientRepository.findById(patientId)
                .orElseThrow(() -> new ServiceException(404, "Patient not found."));
        // Performs if.
        if (!doctorUsername.equals(existing.doctorUsername())) {
            // Performs service exception.
            throw new ServiceException(403, "You cannot delete patients assigned to another doctor.");
        }
        patientRepository.delete(patientId);
    }

    // Finds by id.
    public Optional<Patient> findById(UUID patientId) {
        return patientRepository.findById(patientId);
    }

    // Fetches by doctor.
    public List<Patient> listByDoctor(String doctorUsername) {
        // Performs if.
        if (isBlank(doctorUsername)) {
            return List.of();
        }
        return patientRepository.findByDoctor(doctorUsername);
    }

    // Fetches all.
    public List<Patient> listAll() {
        return patientRepository.findAll();
    }

    private void validatePatientFields(String name, String email, String phone, Integer age,
                                       String gender, String address) {
        // Performs if.
        if (isBlank(name) || isBlank(email) || isBlank(phone) || isBlank(address) || age == null || isBlank(gender)) {
            // Performs service exception.
            throw new ServiceException(400, "Patient name, email, phone, address, age, and gender are required.");
        }
    }

    // Performs safe notes.
    private String safeNotes(String notes) {
        return notes == null ? "" : notes.trim();
    }

    // Checks whether blank.
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
