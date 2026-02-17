package app.service;

public class NoopSmsSender implements SmsSender {
    // Performs send.
    @Override
    public boolean send(String phone, String message) {
        return false;
    }
}
