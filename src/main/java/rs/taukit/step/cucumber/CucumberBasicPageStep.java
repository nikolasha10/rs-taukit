package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicPageStep;

public class CucumberBasicPageStep {
    private AllureBasicPageStep page(String pageName) {
        return new AllureBasicPageStep(pageName);
    }

    @И("^дождаться отображения страницы \"(.+)\"$")
    public void waitForPageLoad(String pageName) {
        page(pageName).waitForPageLoad();
    }

    @И("^дождаться отображения всплывающего окна \"(.+)\"$")
    public void waitForPopupWindow(String pageName) {
        page(pageName).waitForPopupWindow();
    }

    @И("^дождаться закрытия всплывающего окна \"(.+)\"$")
    public void waitForPopupClose(String pageName) {
        page(pageName).waitForPopupClose();
    }

    @И("^открыть страницу \"(.+)\"$")
    public void openPage(String pageName) {
        page(pageName).openPage();
    }
}
