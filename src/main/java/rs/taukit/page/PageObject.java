package rs.taukit.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Request;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.element.interfaces.IElement;

import java.util.function.Consumer;

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

    void waitForPopupClose(String pageName);

    PageObject getPopup(String popupName);

    void waitForTimeout(double time);

    boolean isPageLoaded();

    boolean isPageClosed();

    IContainer getCurrentContext();

    void setCurrentContext(Object blockName);

    void replaceCurrentContext(IContainer blockElements);

    IElement dropCurrentContext();

    double getPageTimeout();

    void setPageTimeout(String timeoutString);

    void setPageTimeout(double timeout);

    Exception getThrownException();

    void close();

    void setActive();

    String getCurrentUrl();

    Locator getByCss(String selector);

    void setRequestHandler(Consumer<Request> handler);
}
