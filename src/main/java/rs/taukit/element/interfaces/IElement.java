package rs.taukit.element.interfaces;

import rs.taukit.element.ElementModel;

public interface IElement extends Clickable, CanBeVisible, CanBeEnabled, HasColor {
    <T> T castTo(Class<T> elementType);

    String getSelector();

    String getElementName();

    ElementModel getModel();

    void hover();

    void scrollIntoView(boolean alignToTop);
}
