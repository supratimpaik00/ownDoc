package app.service;

public interface SmsSender {
    // Performs send.
    boolean send(String phone, String message);
}
