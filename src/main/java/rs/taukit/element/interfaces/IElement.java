package rs.taukit.element.interfaces;

import rs.taukit.driver.IWebDriver;
import rs.taukit.element.ElementModel;

public interface IElement {
    <T> T castTo(Class<T> elementType);

    String getLocator();

    boolean isVisible();

    void click();

    IWebDriver getDriver();

    String getText();

    String getElementName();

    ElementModel getModel();

    boolean isEnabled();

    String getBgColor();

    boolean hasBgColor(String colorRGB);
}
