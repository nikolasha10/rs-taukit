package rs.taukit.exceptions;

import rs.taukit.utils.TauLogger;

/**
 * Исключение "Не удалось дождаться закрытия страницы".
 */
public class PageNotClosedException extends RuntimeException {

    public PageNotClosedException(Throwable cause, String message) {
        TauLogger.fail(message, cause);
    }

}
