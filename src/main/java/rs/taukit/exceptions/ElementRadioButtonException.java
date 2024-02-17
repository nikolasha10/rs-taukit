package rs.taukit.exceptions;

import java.util.function.Supplier;

import static rs.taukit.utils.BasicUtils.page;

/**
 * Исключения при работе с радио-кнопкой
 */
public class ElementRadioButtonException extends RuntimeException {

    /**
     * Конструктор.
     *
     * @param cause   исключение
     * @param message сообщение, описывающее ошибку
     */
    public ElementRadioButtonException(Throwable cause, String message) {
        super(message, cause);
    }

    public ElementRadioButtonException() {
        throw new ElementRadioButtonException(this.getCause(), "");
    }

    public ElementRadioButtonException(String format, Object... args) {
        String message = String.format(format, args);
        throw new ElementRadioButtonException(this.getCause(), message);
    }

    public ElementRadioButtonException(Throwable e, String format, Object... args) {
        String message = String.format(format, args);
        throw new ElementRadioButtonException(e, message);
    }

    public static Supplier<RuntimeException> valueNotFound(String elementName, String value) {
        return () -> new RuntimeException(String.format(
                "Не удалось найти значение '%s' среди возможных значений радио-кнопки '%s' на странице '%s'",
                value, elementName, page().getPageName()));
    }

    public static Supplier<RuntimeException> itemNotFound(String elementName, String itemType, Throwable cause) {
        return () -> new RuntimeException(String.format(
                "Не удалось найти %s, соответствующий заданным условиям, в списке '%s' на странице '%s'",
                itemType, elementName, page().getPageName()), cause);
    }
}
