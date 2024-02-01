package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicBrowserStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicBrowserStep {
    private AllureBasicBrowserStep browserStep() {
        return new AllureBasicBrowserStep();
    }

    @И("^открыть браузер$")
    public void openBrowser() {
        browserStep().openBrowser();
    }

    @И("^открыть ещё один браузер$")
    public void openAnotherOneBrowser() {
        browserStep().openAnotherOneBrowser();
    }

    @И("^продолжить работу в открытом ранее браузере$")
    public void continueWithBrowser() {
        browserStep().continueWithBrowser();
    }

    @И("^перейти по ссылке \"(.+)\"$")
    public void openUrl(String url) {
        browserStep().navigate(eval(url));
    }

    @И("^обновить страницу$")
    public void reloadPage() {
        browserStep().reloadPage();
    }

    @И("^дождаться отображения страницы \"(.+)\" в новой вкладке$")
    public void waitForPageLoadNewTab(String pageName) {
        browserStep().waitForPageLoadNewTab(pageName);
    }

    @И("^закрыть текущую вкладку$")
    public void closeCurrentTab() {
        browserStep().closeCurrentTab();
    }


}
