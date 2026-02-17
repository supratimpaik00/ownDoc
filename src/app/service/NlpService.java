package app.service;

import app.nlp.MedicationNlp;
import app.nlp.MedicationParseResult;

public class NlpService {
    private final MedicationNlp medicationNlp;

    // Initializes nlp service.
    public NlpService(MedicationNlp medicationNlp) {
        this.medicationNlp = medicationNlp;
    }

    // Parses medication.
    public MedicationParseResult parseMedication(String transcript) {
        String raw = transcript == null ? "" : transcript.trim();
        // Performs if.
        if (raw.isEmpty()) {
            // Performs service exception.
            throw new ServiceException(400, "Transcript is required.");
        }
        return medicationNlp.parse(raw);
    }
}
