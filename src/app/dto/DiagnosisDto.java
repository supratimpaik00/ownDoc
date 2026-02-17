package app.dto;

// Initializes diagnosis dto.
public record DiagnosisDto(String createdAt, String diagnosis, String plan) {
    // Performs to json.
    public String toJson() {
        return "{\"createdAt\":\"" + JsonUtil.escape(createdAt) + "\""
                + ",\"diagnosis\":\"" + JsonUtil.escape(diagnosis) + "\""
                + ",\"plan\":\"" + JsonUtil.escape(plan) + "\"}";
    }
}
