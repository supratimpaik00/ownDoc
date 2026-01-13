package app.nlp;

public record MedicationParseResult(String medication, String dosage, String days) {
    public boolean isEmpty() {
        return (medication == null || medication.isBlank())
                && (dosage == null || dosage.isBlank())
                && (days == null || days.isBlank());
    }
}
