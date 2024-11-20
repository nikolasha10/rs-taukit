package rs.taukit.exceptions;

import java.util.function.Supplier;

public class AutoTestBrokenException extends RuntimeException {
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

    public static Supplier<AutoTestBrokenException> get(String format, Object... args){
        return () -> new AutoTestBrokenException(format, args);
    }
}
