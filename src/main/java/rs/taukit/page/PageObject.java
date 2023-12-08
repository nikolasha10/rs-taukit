package rs.taukit.page;

import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;

public interface PageObject {
    String getPageUrl();

    void loadPage();

    void reload();

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

    IHasBlockElements getCurrentContext();

    void setCurrentContext(String blockName);

    IElement dropCurrentContext();

    double getPageTimeout();

    void setPageTimeout(String timeoutString);

    void setPageTimeout(double timeout);

    int getCurrentTabIndex();

    Exception getThrownException();
}
