package rs.taukit.exceptions;

/**
 * Исключение "Отсутствует конфигурация для базы данных".
 */
public class DBConfigNotFoundException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param dbName название базы данных
     */
    public DBConfigNotFoundException(String dbName) {
        super(String.format("Отсутствует конфигурация для базы данных \"%s\"", dbName));
    }

    /**
     * Конструктор.
     *
     * @param dbName название страницы
     * @param cause  исключение
     */
    public DBConfigNotFoundException(Throwable cause, String dbName) {
        super(String.format("Отсутствует конфигурация для базы данных \"%s\"", dbName), cause);
    }
}
