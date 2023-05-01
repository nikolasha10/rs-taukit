package rs.taukit.exceptions;

import rs.taukit.page.PageObject;

/**
 * Исключение "Элемент не описан в yaml-файле страницы".
 */
public class ElementNotDescribedException extends RuntimeException {
    private PageObject pageObject;

    public ElementNotDescribedException(String elementName, String pageName) {
        super(String.format("Элемент '%s' не описан в yaml-файле страницы '%s'", elementName, pageName));
    }

}
