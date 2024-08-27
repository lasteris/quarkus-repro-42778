package su.lasteris;

public class RestClientException extends RuntimeException {
    private final int status;

    public RestClientException(String message, int status) {
        super(message);
        this.status = status;

    }

    public int getStatus() {
        return status;
    }
}
