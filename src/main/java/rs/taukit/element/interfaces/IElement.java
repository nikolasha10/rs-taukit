package rs.taukit.element.interfaces;

import rs.taukit.driver.WebDriver;

import java.util.Map;

public interface IElement {
    <T> T castTo(Class<T> elementType);
    String getLocator();
    boolean isVisible();
    void click();
    Map<String,String> getElementData();

    WebDriver getDriver();

    String getText();

    String getElementName();
}
