package app.nlp;

// Initializes medication parse result.
public record MedicationParseResult(String medication, String dosage, String days) {
    // Checks whether empty.
    public boolean isEmpty() {
        // Performs return.
        return (medication == null || medication.isBlank())
                && (dosage == null || dosage.isBlank())
                && (days == null || days.isBlank());
    }
}
