package rs.taukit.page;

import rs.taukit.element.interfaces.IElement;

public interface PageObject {
    String getPageUrl();

    void loadPage();

    IElement getElement(String elementName);

    IElement replaceElement(String elementName, Class<? extends IElement> elementType);

    void waitForLoad();

    void waitForClose();

    byte[] takeScreenshot();

    void waitForElement(String elementName, boolean isVisible);

    void waitForPopup(Runnable runnable);

    void waitForTimeout(double time);

    boolean isPageLoaded();

    boolean isPageClosed();

    IElement getCurrentContext();

    void setCurrentContext(String blockName);

    IElement dropCurrentContext();
}
