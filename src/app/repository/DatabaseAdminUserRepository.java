package app.repository;

import app.db.Database;
import app.model.AdminUser;

import java.util.Optional;

public class DatabaseAdminUserRepository implements AdminUserRepository {
    private final Database database;

    // Initializes database admin user repository.
    public DatabaseAdminUserRepository(Database database) {
        this.database = database;
    }

    // Finds by username.
    @Override
    public Optional<AdminUser> findByUsername(String username) {
        return database.getAdminUserByUsername(username);
    }

    // Finds by phone.
    @Override
    public Optional<AdminUser> findByPhone(String phone) {
        return database.getAdminUserByPhone(phone);
    }

    // Performs save.
    @Override
    public void save(AdminUser user) {
        database.saveAdminUser(user);
    }

    // Updates password.
    @Override
    public void updatePassword(String username, String passwordHash) {
        database.updateAdminPassword(username, passwordHash);
    }
}
