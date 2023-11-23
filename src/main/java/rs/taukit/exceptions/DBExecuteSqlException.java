package rs.taukit.exceptions;

/**
 * Исключение "Не удалось выполнить sql-скрипт".
 */
public class DBExecuteSqlException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param dbName      название базы данных
     * @param sqlFileName название sql-файла
     */
    public DBExecuteSqlException(String dbName, String sqlFileName) {
        super(String.format("Не удалось выполнить sql-скрипт из файлй '%s' в базе данных '%s'", sqlFileName, dbName));
    }

    /**
     * Конструктор.
     *
     * @param cause       исключение
     * @param dbName      название страницы
     * @param sqlFileName название sql-файла
     */
    public DBExecuteSqlException(Throwable cause, String dbName, String sqlFileName) {
        super(String.format("Не удалось выполнить sql-скрипт из файлй '%s' в базе данных '%s'", sqlFileName, dbName), cause);
    }
}
