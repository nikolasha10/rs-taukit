package rs.taukit.exceptions;

/**
 * Пользователь с описанием \"%s\" не найден в файле Пользователи.yaml".
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Конструктор.
     * @param cause исключение
     * @param description описание пользователя в файле Пользователи.yaml
     */
    public UserNotFoundException(Throwable cause, String description) {
        super(String.format("Пользователь с описанием \"%s\" не найден", description), cause);
    }
}
