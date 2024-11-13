package rs.taukit.exceptions;

import rs.taukit.element.ElementModel;

import java.util.List;
import java.util.function.Supplier;

/**
 * Исключение "Ошибка при создании элемента".
 */
public class ElementNotBuiltException extends AutoTestBrokenException {
    public ElementNotBuiltException(Throwable cause, String elementName, String className, String structure) {
        super(cause, String.format("Ошибка при создании элемента '%s' класса '%s' по данным %s. " +
                        "Возможно, элемент не описан в yaml-файле текущей страницы " +
                        "или описание имеет неправильную структуру, " +
                        "или его название совпадает с названием другого элемента, найденного в текущем контексте " +
                        "и имеющего другой тип и структуру полей",
                elementName, className, structure));
    }

    public static Supplier<RuntimeException> searchMethodNotFound(String elementName, ElementModel elementModel) {
        String message = String.format("""
                Не удалось создать элемент '%s'. Не указан метод поиска элемента (test-id, xpath).
                Модель: %s""", elementName, elementModel);
        return () -> new RuntimeException(message);
    }

    public static Supplier<? extends RuntimeException> modelPrototypeNotFound(String elementName, String elementTypeName) {
        String message = String.format("Не удалось создать элемент '%s'. Не найден прототип модели '%s'",
                elementName, elementTypeName);
        return () -> new RuntimeException(message);

    }

    public static Supplier<? extends RuntimeException> modelIncorrect(List<String> brokenElements) {
        String message = String.format("Описание элементов имеет неправильную структуру: %s", brokenElements);
        return () -> new RuntimeException(message);

    }
}
