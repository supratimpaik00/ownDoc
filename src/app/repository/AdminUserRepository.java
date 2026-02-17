package app.repository;

import app.model.AdminUser;

import java.util.Optional;

public interface AdminUserRepository {
    // Finds by username.
    Optional<AdminUser> findByUsername(String username);

    // Finds by phone.
    Optional<AdminUser> findByPhone(String phone);

    // Performs save.
    void save(AdminUser user);

    // Updates password.
    void updatePassword(String username, String passwordHash);
}
