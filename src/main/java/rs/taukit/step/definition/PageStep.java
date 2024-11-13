package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.PageManager;

public class PageStep extends PageManager {
    @И("^дождаться отображения страницы \"(.+)\"$")
    public void waitForPageLoad(String pageName) {
        super.waitForPageLoad(pageName);
    }

    @И("^дождаться отображения всплывающего окна \"(.+)\"$")
    public void waitForPopupWindow(String pageName) {
        super.waitForPopupWindow(pageName);
    }

    @И("^дождаться закрытия всплывающего окна \"(.+)\"$")
    public void waitForPopupClose(String pageName) {
        super.waitForPopupClose(pageName);
    }

    @И("^открыть страницу \"(.+)\"$")
    public void openPage(String pageName) {
        super.openPage(pageName);
    }
}
