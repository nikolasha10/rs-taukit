package rs.taukit.exceptions;

/**
 * Ошибка при обработке тестовых данных
 */
public class TestDataException extends AutoTestBrokenException {

    /**
     * Конструктор.
     *
     * @param cause   исключение
     * @param message сообщение, описывающее ошибку
     */
    public TestDataException(Throwable cause, String message) {
        super(String.format("Ошибка при обработке тестовых данных: %s", message), cause);
    }

    public TestDataException() {
        throw new TestDataException(this.getCause(), "");
    }

    public TestDataException(String format, Object... args) {
        String message = String.format(format, args);
        throw new TestDataException(this.getCause(), message);
    }

    public TestDataException(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        throw new TestDataException(e, message);
    }
}
