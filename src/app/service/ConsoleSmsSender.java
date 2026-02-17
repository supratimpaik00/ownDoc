package app.service;

public class ConsoleSmsSender implements SmsSender {
    // Performs send.
    @Override
    public boolean send(String phone, String message) {
        System.out.println("--- SMS ---");
        System.out.println("To: " + phone);
        System.out.println(message);
        System.out.println("-----------");
        return true;
    }
}
