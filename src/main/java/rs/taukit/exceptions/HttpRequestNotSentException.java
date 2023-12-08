package rs.taukit.exceptions;

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
    public HttpRequestNotSentException(Throwable cause, String name, String type) {
        super(String.format("Не удалось отправить %s-запрос '%s'", type, name), cause);
    }
}
