package app.db;

import app.model.DiagnosisSession;
import app.model.Doctor;
import app.model.Patient;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcDatabase implements Database {
    private final HikariDataSource dataSource;

    public JdbcDatabase(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        if (user != null && !user.isBlank()) {
            config.setUsername(user);
        }
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }
        config.setMaximumPoolSize(5);
        config.setPoolName("own-doc-db");
        this.dataSource = new HikariDataSource(config);
        runMigrations();
    }

    private void runMigrations() {
        String migrations = Path.of("db", "migration").toAbsolutePath().toString();
        Flyway.configure()
                .dataSource(dataSource)
                .locations("filesystem:" + migrations)
                .load()
                .migrate();
    }

    @Override
    public Optional<Doctor> getDoctor(String username) {
        String sql = "select username, name, password_hash, qualifications from doctors where username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Doctor doctor = new Doctor(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("password_hash"),
                        rs.getString("qualifications")
                );
                return Optional.of(doctor);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch doctor", e);
        }
    }

    @Override
    public void saveDoctor(Doctor doctor) {
        String sql = "insert into doctors (username, name, password_hash, qualifications) values (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doctor.username());
            ps.setString(2, doctor.name());
            ps.setString(3, doctor.passwordHash());
            ps.setString(4, doctor.qualifications());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save doctor", e);
        }
    }

    @Override
    public void savePatient(Patient patient) {
        String sql = "insert into patients (id, name, email, phone, age, gender, address, notes, doctor_username, delivery_status) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, patient.id());
            ps.setString(2, patient.name());
            ps.setString(3, patient.email());
            ps.setString(4, patient.phone());
            ps.setObject(5, patient.age());
            ps.setString(6, patient.gender());
            ps.setString(7, patient.address());
            ps.setString(8, patient.notes());
            ps.setString(9, patient.doctorUsername());
            ps.setString(10, patient.deliveryStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save patient", e);
        }
    }

    @Override
    public void updatePatient(Patient patient) {
        String sql = "update patients set name = ?, email = ?, phone = ?, age = ?, gender = ?, address = ?, notes = ?, doctor_username = ?, delivery_status = ? where id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patient.name());
            ps.setString(2, patient.email());
            ps.setString(3, patient.phone());
            ps.setObject(4, patient.age());
            ps.setString(5, patient.gender());
            ps.setString(6, patient.address());
            ps.setString(7, patient.notes());
            ps.setString(8, patient.doctorUsername());
            ps.setString(9, patient.deliveryStatus());
            ps.setObject(10, patient.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update patient", e);
        }
    }

    @Override
    public void deletePatient(UUID id) {
        String sql = "delete from patients where id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete patient", e);
        }
    }

    @Override
    public Optional<Patient> getPatient(UUID id) {
        String sql = "select id, name, email, phone, age, gender, address, notes, doctor_username, delivery_status from patients where id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(readPatient(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch patient", e);
        }
    }

    @Override
    public List<Patient> getPatients() {
        String sql = "select id, name, email, phone, age, gender, address, notes, doctor_username, delivery_status from patients order by name asc";
        List<Patient> patients = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                patients.add(readPatient(rs));
            }
            return List.copyOf(patients);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list patients", e);
        }
    }

    @Override
    public List<Patient> getPatientsByDoctor(String username) {
        String sql = "select id, name, email, phone, age, gender, address, notes, doctor_username, delivery_status from patients where doctor_username = ? order by name asc";
        List<Patient> patients = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    patients.add(readPatient(rs));
                }
            }
            return List.copyOf(patients);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list patients by doctor", e);
        }
    }

    @Override
    public List<Doctor> getDoctors() {
        String sql = "select username, name, password_hash, qualifications from doctors order by name asc";
        List<Doctor> doctors = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doctors.add(new Doctor(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("password_hash"),
                        rs.getString("qualifications")
                ));
            }
            return List.copyOf(doctors);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list doctors", e);
        }
    }

    @Override
    public void saveDiagnosisSession(DiagnosisSession session) {
        String sql = "insert into diagnosis_sessions (id, patient_id, diagnosis, plan, created_at) values (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, session.id());
            ps.setObject(2, session.patientId());
            ps.setString(3, session.diagnosis());
            ps.setString(4, session.plan());
            ps.setTimestamp(5, Timestamp.valueOf(session.createdAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save diagnosis session", e);
        }
    }

    @Override
    public List<DiagnosisSession> getDiagnosisSessions(UUID patientId) {
        String sql = "select id, patient_id, diagnosis, plan, created_at from diagnosis_sessions where patient_id = ? order by created_at desc";
        List<DiagnosisSession> sessions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sessions.add(readSession(rs));
                }
            }
            return List.copyOf(sessions);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch diagnosis sessions", e);
        }
    }

    private Patient readPatient(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        Integer age = rs.getObject("age", Integer.class);
        String gender = rs.getString("gender");
        String address = rs.getString("address");
        String notes = rs.getString("notes");
        String doctorUsername = rs.getString("doctor_username");
        String deliveryStatus = rs.getString("delivery_status");
        return new Patient(id, name, email, phone, age, gender, address, notes, doctorUsername, deliveryStatus);
    }

    private DiagnosisSession readSession(ResultSet rs) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        UUID patientId = rs.getObject("patient_id", UUID.class);
        String diagnosis = rs.getString("diagnosis");
        String plan = rs.getString("plan");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts == null ? LocalDateTime.now() : ts.toLocalDateTime();
        return new DiagnosisSession(id, patientId, diagnosis, plan, createdAt);
    }
}
