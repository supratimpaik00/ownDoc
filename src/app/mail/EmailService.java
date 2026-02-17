package app.mail;

public interface EmailService {
    // Performs send email.
    void sendEmail(String to, String subject, String body);
}
