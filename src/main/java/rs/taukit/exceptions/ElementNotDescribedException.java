package rs.taukit.exceptions;

import rs.taukit.element.interfaces.IHasBlockElements;

import java.util.List;

/**
 * Исключение "Элемент не описан в yaml-файле страницы".
 */
public class ElementNotDescribedException extends RuntimeException {
    public ElementNotDescribedException(String elementName, String pageName) {
        super(String.format("Элемент \"%s\" не описан в yaml-файле страницы \"%s\"", elementName, pageName));
    }

    public ElementNotDescribedException(List<String> elementList, String pageName) {
        super(String.format("Элементы \"%s\" не описаны в yaml-файле страницы \"%s\"",
                String.join(", ", elementList), pageName));
    }

    public ElementNotDescribedException(String elementName, String pageName, IHasBlockElements blockElements) {
        super(String.format("Элемент \"%s\" не описан в yaml-файле страницы \"%s\" в блоке \"%s\"",
                elementName, pageName, blockElements.getElementName()));
    }

}
