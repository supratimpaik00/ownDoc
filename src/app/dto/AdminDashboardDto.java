package app.dto;

import java.util.List;

public record AdminDashboardDto(AdminSummaryDto summary, List<DoctorSummaryDto> doctors,
                                DoctorSummaryDto selectedDoctor, List<PatientDto> patients) {
    // Performs to json.
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":true,");
        sb.append("\"summary\":").append(summary == null ? "null" : summary.toJson()).append(",");
        sb.append("\"doctors\":[");
        // Performs if.
        if (doctors != null) {
            for (int i = 0; i < doctors.size(); i++) {
                // Performs if.
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(doctors.get(i).toJson());
            }
        }
        sb.append("],");
        sb.append("\"selectedDoctor\":");
        sb.append(selectedDoctor == null ? "null" : selectedDoctor.toJson());
        sb.append(",");
        sb.append("\"patients\":[");
        // Performs if.
        if (patients != null) {
            for (int i = 0; i < patients.size(); i++) {
                // Performs if.
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(patients.get(i).toJson());
            }
        }
        sb.append("]}");
        return sb.toString();
    }
}
