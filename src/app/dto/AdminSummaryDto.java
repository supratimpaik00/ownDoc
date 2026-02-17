package app.dto;

// Initializes admin summary dto.
public record AdminSummaryDto(int totalDoctors, long activeDoctors, int totalPatients) {
    // Performs to json.
    public String toJson() {
        return "{\"totalDoctors\":" + totalDoctors
                + ",\"activeDoctors\":" + activeDoctors
                + ",\"totalPatients\":" + totalPatients + "}";
    }
}
