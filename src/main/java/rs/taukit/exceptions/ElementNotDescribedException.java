package rs.taukit.exceptions;

/**
 * Исключение "Элемент не описан в yaml-файле страницы".
 */
public class ElementNotDescribedException extends RuntimeException {
    public ElementNotDescribedException(String elementName, String pageName) {
        super(String.format("Элемент '%s' не описан в yaml-файле страницы '%s'", elementName, pageName));
    }

}
