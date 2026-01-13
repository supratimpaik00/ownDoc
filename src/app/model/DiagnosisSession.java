package app.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiagnosisSession(UUID id, UUID patientId, String diagnosis, String plan, LocalDateTime createdAt) {}
