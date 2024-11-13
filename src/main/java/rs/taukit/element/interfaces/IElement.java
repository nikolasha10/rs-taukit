package rs.taukit.element.interfaces;

import rs.taukit.element.ElementModel;

public interface IElement {
    <T> T castTo(Class<T> elementType);

    String getSelector();

    boolean isVisible();

    void click();

    String getText();

    String getElementName();

    ElementModel getModel();

    boolean isDisabled();

    boolean isEnabled();

    String getBgColor();

    boolean hasBgColor(String colorRGB);

    boolean hasText(String value);
}
