package app.model;

import java.util.UUID;

public record Patient(UUID id, String name, String email, String phone, Integer age, String gender, String address, String notes, String doctorUsername, String deliveryStatus) {}
