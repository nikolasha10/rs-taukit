package rs.taukit.exceptions;

import rs.taukit.utils.TauLogger;

/**
 * Исключение "Не удалось дождаться отображения страницы".
 */
public class PageNotLoadException extends RuntimeException {

    public PageNotLoadException(Throwable cause, String message) {
        TauLogger.fail(message, cause);
    }

}
