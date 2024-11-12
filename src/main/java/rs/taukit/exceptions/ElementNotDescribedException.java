package rs.taukit.exceptions;

import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.element.interfaces.IHasTableElements;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Исключение "Элемент не описан в yaml-файле страницы".
 */
public class ElementNotDescribedException extends AutoTestBrokenException {
    public ElementNotDescribedException(String elementName, String pageName) {
        super(String.format("Элемент '%s' не описан в yaml-файле страницы '%s'", elementName, pageName));
    }

    public ElementNotDescribedException(List<String> elementList, String pageName) {
        super(String.format("Элементы '%s' не описаны в yaml-файле страницы '%s'",
                String.join(", ", elementList), pageName));
    }

    public ElementNotDescribedException(String elementName, String pageName, IContainer blockElements) {
        super(String.format("""
                        Элемент '%s' не описан в yaml-файле страницы '%s' в контейнере '%s'.
                        Описаны элементы: %s""",
                elementName, pageName, blockElements.getElementName(),
                blockElements.getElementList().stream().map(IElement::getElementName).collect(Collectors.toList())));
    }

    public ElementNotDescribedException(String elementName, String pageName, IHasTableElements iHasTableElements) {
        super(String.format("""
                        Элемент '%s' не описан в yaml-файле страницы '%s' в таблице '%s'.
                        Структура таблицы: %s""",
                elementName, pageName, iHasTableElements.getElementName(),
                iHasTableElements.getModel()));
    }

}
