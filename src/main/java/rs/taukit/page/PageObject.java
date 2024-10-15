package rs.taukit.page;

import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;

public interface PageObject {
    String getPageUrl();

    String getPageName();

    void loadPage();

    void reload();

    <T extends IElement> T getElement(String elementName);

    void waitForLoad();

    void waitForLoadAndContinue();

    void waitForLoadAndContinue(double timeout);

    void waitForClose();

    byte[] takeScreenshot();

    void waitForElement(String elementName, boolean isVisible);

    void waitForPopup(String pageName);

    void waitForTimeout(double time);

    boolean isPageLoaded();

    boolean isPageClosed();

    IHasBlockElements getCurrentContext();

    void setCurrentContext(Object blockName);

    void replaceCurrentContext(IHasBlockElements blockElements);

    IElement dropCurrentContext();

    double getPageTimeout();

    void setPageTimeout(String timeoutString);

    void setPageTimeout(double timeout);

    Exception getThrownException();

    void close();

    void setActive();

    void waitForPopupClose(String pageName);

    String getCurrentUrl();
}
