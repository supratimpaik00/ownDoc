package app.mail;

public class ConsoleEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("--- Email ---");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println(body);
        System.out.println("------------");
    }
}
