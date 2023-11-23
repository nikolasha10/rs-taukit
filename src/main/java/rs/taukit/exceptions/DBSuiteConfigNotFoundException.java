package rs.taukit.exceptions;

/**
 * Исключение "Не удалось набор параметров конфигурации баз данных".
 */
public class DBSuiteConfigNotFoundException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param dbSuiteName название набора параметров
     */
    public DBSuiteConfigNotFoundException(String dbSuiteName) {
        super(String.format("Отсутствует набор параметров конфигурации баз данных с названием '%s'", dbSuiteName));
    }

    /**
     * Конструктор.
     *
     * @param dbSuiteName название страницы
     * @param cause       исключение
     */
    public DBSuiteConfigNotFoundException(Throwable cause, String dbSuiteName) {
        super(String.format("Отсутствует набор параметров конфигурации баз данных с названием '%s'", dbSuiteName), cause);
    }
}
