package rs.taukit.exceptions;

import java.util.List;
import java.util.function.Supplier;

/**
 * Исключения при работе с выпадающим списком
 */
public class DropdownListException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param cause   исключение
     * @param message сообщение, описывающее ошибку
     */
    public DropdownListException(Throwable cause, String message) {
        super(message, cause);
    }

    public DropdownListException() {
        throw new DropdownListException(this.getCause(), "");
    }

    public DropdownListException(String format, Object... args) {
        String message = String.format(format, args);
        throw new DropdownListException(this.getCause(), message);
    }

    public DropdownListException(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        throw new DropdownListException(e, message);
    }

    public static Supplier<RuntimeException> row(String rowName, String tableName) {
        return () -> new RuntimeException(String.format(
                "Не удалось найти строку (%s) в таблице '%s'", rowName, tableName));
    }

    public static Supplier<RuntimeException> row(String tableName) {
        return () -> new RuntimeException(String.format(
                "Не удалось найти строку в таблице '%s', соответствующую заданным условиям", tableName));

    }

    public static Supplier<RuntimeException> header(String tableName, String headerName, List<String> headerList) {
        return () -> new RuntimeException(String.format(
                "В таблице '%s' нет столбца с именем '%s'. Имеются столбцы: %s", tableName, headerName, headerList));
    }
}
