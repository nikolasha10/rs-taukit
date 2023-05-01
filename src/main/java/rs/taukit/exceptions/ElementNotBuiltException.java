package rs.taukit.exceptions;

/**
 * Исключение "Элемент не описан в yaml-файле страницы".
 */
public class ElementNotBuiltException extends RuntimeException {
    public ElementNotBuiltException(Throwable cause, String elementName, String className, String structure) {
        super(String.format("Ошибка при создании элемента '%s' класса '%s' по данным %s",
                elementName, className, structure), cause);
    }

}
