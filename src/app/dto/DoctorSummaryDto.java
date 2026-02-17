package app.dto;

// Initializes doctor summary dto.
public record DoctorSummaryDto(String username, String name, String qualifications, long patients, String status) {
    // Performs to json.
    public String toJson() {
        return "{\"username\":\"" + JsonUtil.escape(username) + "\""
                + ",\"name\":\"" + JsonUtil.escape(name) + "\""
                + ",\"qualifications\":\"" + JsonUtil.escape(qualifications) + "\""
                + ",\"patients\":" + patients
                + ",\"status\":\"" + JsonUtil.escape(status) + "\"}";
    }
}
