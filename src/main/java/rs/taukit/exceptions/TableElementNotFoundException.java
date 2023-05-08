package rs.taukit.exceptions;

/**
 * Элемент не найден в табли
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
}
