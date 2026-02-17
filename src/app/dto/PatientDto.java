package app.dto;

import java.util.List;

public record PatientDto(String id, String name, String email, String phone, Integer age, String gender,
                         String address, String notes, String deliveryStatus, String deliveryMessage,
                         List<DiagnosisDto> history) {
    // Performs to json.
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(JsonUtil.escape(id)).append("\"");
        sb.append(",\"name\":\"").append(JsonUtil.escape(name)).append("\"");
        sb.append(",\"email\":\"").append(JsonUtil.escape(email)).append("\"");
        sb.append(",\"phone\":\"").append(JsonUtil.escape(phone)).append("\"");
        sb.append(",\"age\":").append(age == null ? "null" : age);
        sb.append(",\"gender\":\"").append(JsonUtil.escape(valueOrEmpty(gender))).append("\"");
        sb.append(",\"address\":\"").append(JsonUtil.escape(valueOrEmpty(address))).append("\"");
        sb.append(",\"notes\":\"").append(JsonUtil.escape(valueOrEmpty(notes))).append("\"");
        sb.append(",\"deliveryStatus\":\"").append(JsonUtil.escape(valueOrEmpty(deliveryStatus))).append("\"");
        sb.append(",\"deliveryMessage\":\"").append(JsonUtil.escape(valueOrEmpty(deliveryMessage))).append("\"");
        sb.append(",\"history\":[");
        // Performs if.
        if (history != null) {
            for (int i = 0; i < history.size(); i++) {
                // Performs if.
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(history.get(i).toJson());
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    // Performs value or empty.
    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
