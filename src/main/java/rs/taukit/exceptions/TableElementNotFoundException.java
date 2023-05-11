package rs.taukit.exceptions;

import rs.taukit.utils.TauLogger;

import java.util.function.Supplier;

/**
 * Элемент не найден в таблице
 */
public class TableElementNotFoundException extends RuntimeException {

    /**
     * Конструктор.
     * @param cause исключение
     * @param message сообщение, описывающее ошибку
     */
    public TableElementNotFoundException(Throwable cause, String message) {
        super(String.format("Ошибка при обработке тестовых данных: %s", message), cause);
    }
    public TableElementNotFoundException() {
        throw new TableElementNotFoundException(this.getCause(), "");
    }
    public TableElementNotFoundException(String format, Object... args) {
        String message = String.format(format, args);
        throw new TableElementNotFoundException(this.getCause(), message);
    }
    public TableElementNotFoundException(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        throw new TableElementNotFoundException(e, message);
    }

    public static Supplier<RuntimeException> row(String rowName, String tableName){
        return () -> {
            TauLogger.fail("Не удалось найти строку (%s) в таблице '%s'", rowName, tableName);
            return null;
        };
    }
}
