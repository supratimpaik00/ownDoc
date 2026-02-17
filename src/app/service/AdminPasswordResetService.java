package app.service;

import app.model.AdminUser;
import app.util.PasswordHasher;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AdminPasswordResetService {
    private static final int OTP_TTL_SECONDS = 300;
    private static final int RESET_TTL_SECONDS = 600;
    private static final int MAX_ATTEMPTS = 5;
    private final AdminUserService adminUserService;
    private final SmsSender smsSender;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpEntry> otpByPhone = new ConcurrentHashMap<>();
    private final Map<String, ResetEntry> resetTokens = new ConcurrentHashMap<>();

    // Initializes admin password reset service.
    public AdminPasswordResetService(AdminUserService adminUserService, SmsSender smsSender) {
        this.adminUserService = adminUserService;
        this.smsSender = smsSender;
    }

    // Performs request otp.
    public void requestOtp(String phone) {
        AdminUser user = adminUserService.findByPhone(phone)
                .orElseThrow(() -> new ServiceException(404, "No admin user registered with this phone."));
        String code = generateOtp();
        otpByPhone.put(phone, new OtpEntry(hash(code), Instant.now().plusSeconds(OTP_TTL_SECONDS), 0, user.username()));
        boolean sent = smsSender != null && smsSender.send(phone, "Your admin reset OTP is: " + code);
        // Performs if.
        if (!sent) {
            // Performs service exception.
            throw new ServiceException(503, "SMS provider not configured.");
        }
    }

    // Performs verify otp.
    public String verifyOtp(String phone, String code) {
        OtpEntry entry = otpByPhone.get(phone);
        // Performs if.
        if (entry == null || entry.expiresAt.isBefore(Instant.now())) {
            otpByPhone.remove(phone);
            // Performs service exception.
            throw new ServiceException(400, "OTP expired.");
        }
        // Performs if.
        if (entry.attempts >= MAX_ATTEMPTS) {
            otpByPhone.remove(phone);
            // Performs service exception.
            throw new ServiceException(400, "OTP attempts exceeded.");
        }
        // Performs if.
        if (!entry.codeHash.equals(hash(code))) {
            otpByPhone.put(phone, entry.incrementAttempts());
            // Performs service exception.
            throw new ServiceException(400, "Invalid OTP.");
        }
        otpByPhone.remove(phone);
        String token = UUID.randomUUID().toString();
        resetTokens.put(token, new ResetEntry(entry.username, Instant.now().plusSeconds(RESET_TTL_SECONDS)));
        return token;
    }

    // Performs reset password.
    public void resetPassword(String token, String newPassword) {
        ResetEntry entry = resetTokens.get(token);
        // Performs if.
        if (entry == null || entry.expiresAt.isBefore(Instant.now())) {
            resetTokens.remove(token);
            // Performs service exception.
            throw new ServiceException(400, "Reset token expired.");
        }
        // Performs if.
        if (newPassword == null || newPassword.trim().isEmpty()) {
            // Performs service exception.
            throw new ServiceException(400, "New password is required.");
        }
        adminUserService.updatePassword(entry.username, newPassword.trim());
        resetTokens.remove(token);
    }

    // Performs generate otp.
    private String generateOtp() {
        int value = 100000 + random.nextInt(900000);
        return Integer.toString(value);
    }

    // Performs hash.
    private String hash(String value) {
        return PasswordHasher.hash(value == null ? "" : value.trim());
    }

    // Performs otp entry.
    private record OtpEntry(String codeHash, Instant expiresAt, int attempts, String username) {
        // Performs increment attempts.
        OtpEntry incrementAttempts() {
            // Performs otp entry.
            return new OtpEntry(codeHash, expiresAt, attempts + 1, username);
        }
    }

    private record ResetEntry(String username, Instant expiresAt) {}
}
