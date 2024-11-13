package rs.taukit.exceptions;

/**
 * Драйвер не найден".
 */
public class DriverNotFoundException extends AutoTestBrokenException {

    /**
     * Конструктор.
     *
     * @param cause      исключение
     * @param driverName описание пользователя в файле Пользователи.yaml
     */
    public DriverNotFoundException(Throwable cause, String driverName) {
        super(String.format("Драйвер '%s' не найден", driverName), cause);
    }
}
