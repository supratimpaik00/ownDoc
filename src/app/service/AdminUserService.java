package app.service;

import app.model.AdminUser;
import app.repository.AdminUserRepository;
import app.util.PasswordHasher;

import java.util.Optional;

public class AdminUserService {
    private final AdminUserRepository repository;

    // Initializes admin user service.
    public AdminUserService(AdminUserRepository repository) {
        this.repository = repository;
    }

    // Finds by username.
    public Optional<AdminUser> findByUsername(String username) {
        // Performs if.
        if (isBlank(username)) {
            return Optional.empty();
        }
        return repository.findByUsername(username.trim());
    }

    // Finds by phone.
    public Optional<AdminUser> findByPhone(String phone) {
        // Performs if.
        if (isBlank(phone)) {
            return Optional.empty();
        }
        return repository.findByPhone(phone.trim());
    }

    // Creates if missing.
    public AdminUser createIfMissing(String username, String rawPassword, String phone) {
        Optional<AdminUser> existing = findByUsername(username);
        // Performs if.
        if (existing.isPresent()) {
            return existing.get();
        }
        AdminUser user = new AdminUser(username.trim(), PasswordHasher.hash(rawPassword.trim()), phone.trim());
        repository.save(user);
        return user;
    }

    // Updates password.
    public void updatePassword(String username, String rawPassword) {
        // Performs if.
        if (isBlank(username) || isBlank(rawPassword)) {
            // Performs service exception.
            throw new ServiceException(400, "Username and password are required.");
        }
        repository.updatePassword(username.trim(), PasswordHasher.hash(rawPassword.trim()));
    }

    // Checks whether blank.
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
