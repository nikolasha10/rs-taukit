package rs.taukit.page;

import rs.taukit.element.interfaces.IElement;

import java.util.Map;

public interface PageObject {

    void loadPage();

    IElement getElement(String elementName);
    IElement replaceElement(String elementName, Class<? extends IElement> elementType);

    void waitForLoad();

    byte[] takeScreenshot();

    void waitForElement(String elementName, boolean isVisible);

    void waitForPopup(Runnable runnable);

    void waitForTimeout(double time);

    boolean isPageLoaded();
}
