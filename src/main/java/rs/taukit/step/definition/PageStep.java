package rs.taukit.step.definition;

import io.cucumber.java.ru.И;
import rs.taukit.step.manager.PageManager;

import static rs.taukit.utils.BasicUtils.eval;

public class PageStep extends PageManager {
    @И("^дождаться отображения страницы \"(.+)\"$")
    public void waitForPageLoad(String pageName) {
        super.waitForPageLoad(eval(pageName));
    }

    @И("^дождаться отображения всплывающего окна \"(.+)\"$")
    public void waitForPopupWindow(String pageName) {
        super.waitForPopupWindow(eval(pageName));
    }

    @И("^дождаться закрытия всплывающего окна \"(.+)\"$")
    public void waitForPopupClose(String pageName) {
        super.waitForPopupClose(eval(pageName));
    }

    @И("^открыть страницу \"(.+)\"$")
    public void openPage(String pageName) {
        super.openPage(eval(pageName));
    }

    @И("^сохранить текущий url, как \"(.+)\"$")
    public void saveCurrentUrl(String parameterName) {
        super.saveCurrentUrl(parameterName);
    }
}
