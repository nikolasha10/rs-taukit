package rs.taukit.records;

import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;

public record CheckedElement(IElement element, Class<? extends IElement> elementType, Object expectedValue) {
    public CheckedElement(IElement element, String elementType, Object expectedValue) {
        this(element, ElementType.getType(elementType).getElementClass(), expectedValue);
    }
}
