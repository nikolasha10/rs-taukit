package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.BrowserManager;

import static rs.taukit.utils.BasicUtils.eval;

public class BrowserStep extends BrowserManager {

    @И("^открыть браузер$")
    public void openBrowser() {
        super.openBrowser();
    }

    @И("^открыть браузер с параметрами:$")
    public void openBrowser(DataTable dataTable) {
        super.openBrowser(eval(dataTable));
    }

    @И("^открыть ещё один браузер$")
    public void openAnotherOneBrowser() {
        super.openAnotherOneBrowser();
    }

    @И("^продолжить работу в открытом ранее браузере$")
    public void continueWithBrowser() {
        super.continueWithBrowser();
    }

    @И("^продолжить работу в браузере № (\\d+)$")
    public void continueWithBrowserNumber(int browserNumber) {
        super.continueWithBrowserNumber(browserNumber);
    }

    @И("^перейти по ссылке \"(.+)\"$")
    public void openUrl(String url) {
        super.navigate(eval(url));
    }

    @И("^обновить страницу$")
    public void reloadPage() {
        super.reloadPage();
    }

    @И("^дождаться отображения страницы \"(.+)\" в новой вкладке$")
    public void waitForPageLoadNewTab(String pageName) {
        super.waitForPageLoadNewTab(pageName);
    }

    @И("^закрыть текущую вкладку$")
    public void closeCurrentTab() {
        super.closeCurrentTab();
    }

    @И("^закрыть вкладку (\\d+)$")
    public void closeTab(int tabNumber) {
        super.closeTab(tabNumber);
    }
}
