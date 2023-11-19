package rs.taukit.element.interfaces;

import rs.taukit.driver.WebDriver;
import rs.taukit.element.ElementModel;

public interface IElement {
    <T> T castTo(Class<T> elementType);

    String getLocator();

    boolean isVisible();

    void click();

    WebDriver getDriver();

    String getText();

    String getElementName();

    ElementModel getModel();
}
