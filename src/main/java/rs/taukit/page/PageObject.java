package rs.taukit.page;

import rs.taukit.element.IElement;

public interface PageObject {

    void loadPage();

    IElement getElement(String elementName);
    IElement replaceElement(String elementName, Class<? extends IElement> elementType);

    void waitForLoad();

    byte[] takeScreenshot();

    void waitForElement(String elementName, boolean isVisible);

    void waitForTimeout(double time);

    boolean isPageLoaded();
}
