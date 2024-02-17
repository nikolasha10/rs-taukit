package rs.taukit.records;

import rs.taukit.element.interfaces.IElement;

/**
 * Объект, содержащий параметры проверяемого элемента
 *
 * @param element       - проверяемый элемент
 * @param expectedValue - ожидаемое значение (тип зависит от метода проверки)
 */
public record CheckedElement(IElement element, Object expectedValue) {
}
