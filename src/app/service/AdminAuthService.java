package app.service;

public class AdminAuthService {
    private final String adminUser;
    private final String adminPass;
    private final String adminPhone;
    private final AdminUserService adminUserService;

    // Initializes admin auth service.
    public AdminAuthService(AdminUserService adminUserService) {
        this.adminUser = resolveAdminUser();
        this.adminPass = resolveAdminPass();
        this.adminPhone = resolveAdminPhone();
        this.adminUserService = adminUserService;
    }

    // Performs validate.
    public void validate(String username, String password) {
        // Performs if.
        if (isBlank(username) || isBlank(password)) {
            // Performs service exception.
            throw new ServiceException(401, "Invalid credentials.");
        }
        // Performs if.
        String trimmedUser = username.trim();
        String trimmedPass = password.trim();
        java.util.Optional<app.model.AdminUser> stored = adminUserService == null
                ? java.util.Optional.empty()
                : adminUserService.findByUsername(trimmedUser);
        // Performs if.
        if (stored.isPresent()) {
            // Performs if.
            if (!stored.get().passwordHash().equals(app.util.PasswordHasher.hash(trimmedPass))) {
                // Performs service exception.
                throw new ServiceException(401, "Invalid credentials.");
            }
            return;
        }
        // Performs if.
        if (!adminUser.equals(trimmedUser) || !adminPass.equals(trimmedPass)) {
            // Performs service exception.
            throw new ServiceException(401, "Invalid credentials.");
        }
        // Performs if.
        if (adminUserService != null && adminPhone != null && !adminPhone.isBlank()) {
            adminUserService.createIfMissing(adminUser, adminPass, adminPhone);
        }
    }

    // Resolves admin user.
    private String resolveAdminUser() {
        String user = System.getenv("ADMIN_USER");
        // Performs if.
        if (user == null || user.isBlank()) {
            System.out.println("ADMIN_USER not set; defaulting to admin.");
            return "admin";
        }
        return user;
    }

    // Resolves admin pass.
    private String resolveAdminPass() {
        String pass = System.getenv("ADMIN_PASS");
        // Performs if.
        if (pass == null || pass.isBlank()) {
            System.out.println("ADMIN_PASS not set; defaulting to adminpass.");
            return "adminpass";
        }
        return pass;
    }

    // Resolves admin phone.
    private String resolveAdminPhone() {
        String phone = System.getenv("ADMIN_PHONE");
        // Performs if.
        if (phone == null || phone.isBlank()) {
            return "";
        }
        return phone.trim();
    }

    // Checks whether blank.
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
