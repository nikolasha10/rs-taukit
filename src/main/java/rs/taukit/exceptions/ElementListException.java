package rs.taukit.exceptions;

import java.util.function.Supplier;

import static rs.taukit.utils.BasicUtils.page;

/**
 * Исключения при работе с выпадающим списком
 */
public class ElementListException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param cause   исключение
     * @param message сообщение, описывающее ошибку
     */
    public ElementListException(Throwable cause, String message) {
        super(message, cause);
    }

    public ElementListException() {
        throw new ElementListException(this.getCause(), "");
    }

    public ElementListException(String format, Object... args) {
        String message = String.format(format, args);
        throw new ElementListException(this.getCause(), message);
    }

    public ElementListException(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        throw new ElementListException(e, message);
    }

    public static Supplier<RuntimeException> itemNotFound(String elementName, String itemType, Throwable cause) {
        return () -> new RuntimeException(String.format(
                "Не удалось найти %s, соответствующий заданным условиям, в списке '%s' на странице '%s'",
                itemType, elementName, page().getPageName()), cause);
    }
}
