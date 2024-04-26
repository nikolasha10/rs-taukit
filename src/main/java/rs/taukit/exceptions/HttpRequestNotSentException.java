package rs.taukit.exceptions;

import rs.taukit.utils.TauLogger;

/**
 * Не удалось отправить HTTP-запрос".
 */
public class HttpRequestNotSentException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param cause исключение
     * @param name  описание пользователя в файле Пользователи.yaml
     */
    public HttpRequestNotSentException(Throwable cause, String name, String type, int status) {
        TauLogger.fail(cause, "Не удалось отправить %s-запрос \"%s\". Сервер вернул статус %d", type, name, status);
    }
}
