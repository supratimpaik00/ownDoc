package app.service;

import app.model.Patient;
import app.repository.PatientRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;

public class DeliveryService {
    private final PatientRepository patientRepository;
    private final String tokenSecret;

    // Initializes delivery service.
    public DeliveryService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
        this.tokenSecret = resolveTokenSecret();
    }

    // Builds message.
    public String buildMessage(String baseUrl, UUID patientId) {
        String token = createToken(patientId);
        String link = baseUrl + "/delivery/confirm?patient=" + patientId + "&token=" + token;
        return "Do want ur medicine delivered? Tap this link: " + link;
    }

    // Checks whether valid token.
    public boolean isValidToken(UUID patientId, String token) {
        // Performs if.
        if (token == null || token.isBlank()) {
            return false;
        }
        String expected = createToken(patientId);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
    }

    // Performs record response.
    public Patient recordResponse(UUID patientId, String choice) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ServiceException(404, "Patient not found."));
        Patient updated = new Patient(
                patient.id(),
                patient.name(),
                patient.email(),
                patient.phone(),
                patient.age(),
                patient.gender(),
                patient.address(),
                patient.notes(),
                patient.doctorUsername(),
                choice
        );
        patientRepository.update(updated);
        return updated;
    }

    // Creates token.
    public String createToken(UUID patientId) {
        // Performs hmac sha 256 hex.
        return hmacSha256Hex(patientId.toString(), tokenSecret);
    }

    // Performs hmac sha 256 hex.
    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] result = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            // Performs for.
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // Performs runtime exception.
            throw new RuntimeException("Failed to generate delivery token", e);
        }
    }

    // Resolves token secret.
    private String resolveTokenSecret() {
        String secret = System.getenv("DELIVERY_TOKEN_SECRET");
        // Performs if.
        if (secret == null || secret.isBlank()) {
            System.out.println("DELIVERY_TOKEN_SECRET not set; using a development default.");
            return "dev-secret";
        }
        return secret;
    }
}
