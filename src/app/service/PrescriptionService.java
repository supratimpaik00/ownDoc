package app.service;

import app.mail.EmailService;
import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrescriptionService {
    private final EmailService emailService;
    private final DiagnosisService diagnosisService;

    // Initializes prescription service.
    public PrescriptionService(EmailService emailService, DiagnosisService diagnosisService) {
        this.emailService = emailService;
        this.diagnosisService = diagnosisService;
    }

    // Performs send prescription.
    public DiagnosisSession sendPrescription(Doctor doctor, Patient patient, String diagnosis, String plan) {
        // Performs if.
        if (doctor == null || patient == null) {
            // Performs service exception.
            throw new ServiceException(400, "Doctor and patient are required.");
        }
        // Performs if.
        if (isBlank(plan)) {
            // Performs service exception.
            throw new ServiceException(400, "Medication plan is required.");
        }
        String subject = "Prescription for " + patient.name();
        String body = "Prescribed by: Dr. " + doctor.name() + " (" + doctor.qualifications() + ")\n"
                + "Diagnosis: " + (isBlank(diagnosis) ? "N/A" : diagnosis.trim())
                + "\nPlan:\n" + plan.trim()
                + "\nPrescribed at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        emailService.sendEmail(patient.email(), subject, body);
        return diagnosisService.saveSession(patient.id(), isBlank(diagnosis) ? "N/A" : diagnosis.trim(), plan.trim());
    }

    // Checks whether blank.
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
