package rs.taukit.exceptions;

public abstract class AutoTestBrokenException extends RuntimeException {
    public AutoTestBrokenException() {
        super();
    }

    public AutoTestBrokenException(String message) {
        super(message);
    }
    public AutoTestBrokenException(String format, Object... args) {
        super(String.format(format, args));
    }

    public AutoTestBrokenException(Throwable cause) {
        super(cause);
    }

    public AutoTestBrokenException(Throwable cause, String format, Object... args) {
        super(String.format(format, args), cause);
    }


}
