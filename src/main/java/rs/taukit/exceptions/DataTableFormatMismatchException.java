package rs.taukit.exceptions;

/**
 * Ошибка при несоответствии формата таблицы данных ожидаемому
 */
public class DataTableFormatMismatchException extends AutoTestBrokenException {

    public DataTableFormatMismatchException(String message) {
        super(message);
    }

    public DataTableFormatMismatchException(String pattern, Object... args) {
        String message = String.format(pattern, args);
        throw new DataTableFormatMismatchException(message);
    }
}
