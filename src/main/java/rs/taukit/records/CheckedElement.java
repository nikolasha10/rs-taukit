package rs.taukit.records;

import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;

/**
 * Объект, содержащий параметры проверяемого элемента
 *
 * @param element       - проверяемый элемент
 * @param elementType   - название типа элемента
 * @param expectedValue - ожидаемое значение (тип зависит от метода проверки)
 */
public record CheckedElement(IElement element, Class<? extends IElement> elementType, Object expectedValue) {
    /**
     * Объект, содержащий параметры проверяемого элемента
     *
     * @param element       - проверяемый элемент
     * @param elementType   - название типа элемента
     * @param expectedValue - ожидаемое значение (тип зависит от метода проверки)
     */
    public CheckedElement(IElement element, String elementType, Object expectedValue) {
        this(element, ElementType.getType(elementType).getElementClass(), expectedValue);
    }
}
