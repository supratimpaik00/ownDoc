package app.service;

public class ServiceException extends RuntimeException {
    private final int status;

    // Initializes service exception.
    public ServiceException(int status, String message) {
        // Performs super.
        super(message);
        this.status = status;
    }

    // Performs status.
    public int status() {
        return status;
    }
}
