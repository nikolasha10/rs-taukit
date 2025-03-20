package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.page.PageObject;
import rs.taukit.utils.TauLogger;

public class BrowserManager extends StepManager {
    private final IWebDriver driver;
    private IWebBrowser browser;

    public BrowserManager() {
        this.driver = getDriver();
        this.browser = getBrowser();
    }

    @Step("открыть браузер")
    public void openBrowser() {
        if (!driver.isBrowserOpen()) {
            driver.openBrowser();
            this.browser = getBrowser();
        }
        TauLogger.report("Открыт браузер '%s'", driver.getBrowserType());
    }

    @Step("открыть браузер с параметрами:")
    public void openBrowser(DataTable dataTable) {
        if (!driver.isBrowserOpen()) {
            driver.openBrowser(dataTable.asList());
            this.browser = getBrowser();
        }
        TauLogger.report("Открыт браузер '%s'", driver.getBrowserType());
    }

    @Step("открыть ещё один браузер")
    public void openAnotherOneBrowser() {
        driver.openBrowser();
        browser = getBrowser();
        TauLogger.report("Открыт браузер '%s'", driver.getBrowserType());
    }

    @Step("обновить страницу")
    public void reloadPage() {
        String pageName = getPage().getPageName();
        run(() -> getPage().reload(), "Не удалось обновить страницу '%s'", pageName);
        TauLogger.report("Страница '%s' обновлена", pageName);
    }

    @Step("закрыть текущую вкладку")
    public void closeCurrentTab() {
        int tabIndex = browser.getActiveTabIndex();
        run(() -> browser.closeCurrentTab(), "Не удалось закрыть вкладку %d", tabIndex + 1);
        TauLogger.report("Закрыта вкладка %d", tabIndex + 1);
        TauLogger.report("[примечание] открыта вкладка %d", browser.getActiveTabIndex() + 1);
        TauLogger.report("[примечание] открыта страница: %s", getPage().getPageName());
        IContainer currentContext = getPage().getCurrentContext();
        if (!currentContext.getElementName().equals(getPage().getPageName())) {
            TauLogger.report("[примечание] текущий контекст: %s %s", currentContext.getModel().getType(), currentContext.getElementName());
        }
    }


    public void continueWithBrowser() {
        continueWithBrowser(driver.getCurrentBrowserIndex());
    }

    @Step("продолжить работу в открытом ранее браузере ({browserIndex})")
    public void continueWithBrowser(int browserIndex) {
        driver.switchBrowser(browserIndex - 1);
        browser = getBrowser();
        browser.setActiveTab(browser.getActiveTabIndex());
        PageObject currentPage = browser.getCurrentPage();
        IContainer currentContext = getContext();
        String contextType;
        String contextName;
        if (currentContext == null) {
            contextType = "страница";
            contextName = currentPage.getPageName();
        } else {
            contextType = currentContext.getModel().getType();
            contextName = currentContext.getElementName();
        }
        scenarioStorage.setCurrentContextName("%s '%s'", contextType, contextName);
        TauLogger.report("Активен браузер: %d", driver.getCurrentBrowserIndex() + 1);
        TauLogger.report("[примечание] открыта вкладка: %d", browser.getActiveTabIndex() + 1);
        TauLogger.report("[примечание] открыта страница: %s", currentPage.getPageName());
        TauLogger.report("[примечание] текущий контекст: %s '%s'", contextType, contextName);
    }

    @Step("дождаться отображения страницы '{pageName}' в новой вкладке")
    public void waitForPageLoadNewTab(String pageName) {
        int activeTabIndex = browser.getActiveTabIndex() + 1;
        waitForCondition(() -> browser.setActiveTab(activeTabIndex),
                "Не удалось дождаться открытия новой вкладки браузера");
        TauLogger.report("Открыта вкладка %d", browser.getActiveTabIndex());
        run(() -> browser.getPage(pageName).waitForLoad());
        TauLogger.report("Отображается страница '%s'", pageName);
    }

    @Step("перейти по ссылке {url}")
    public void navigate(String url) {
        run(() -> browser.navigate(url), "Не удалось перейти по ссылке '%s'", url);
        TauLogger.console("Перешли по ссылке '%s'", url);
    }

    @Step("продолжить работу в браузере № {browserNumber}")
    public void continueWithBrowserNumber(int browserNumber) {
        driver.switchBrowser(browserNumber - 1);
        browser = getBrowser();
        TauLogger.report("Активен браузер %d", browserNumber);
        TauLogger.report("[примечание] открыта вкладка %d", getBrowser().getActiveTabIndex() + 1);
        TauLogger.report("[примечание] отображается страница: %s", getBrowser().getCurrentPage().getPageName());
        IContainer currentContext = getContext();
        if (currentContext != null) {
            TauLogger.report("[примечание] текущий контекст: %s '%s'",
                    currentContext.getModel().getType(), currentContext.getElementName());
        }
    }

    @Step("Закрыть вкладку {tabNumber}")
    public void closeTab(int tabNumber) {
        run(() -> browser.closeTab(tabNumber - 1), "Не удалось закрыть вкладку %d", tabNumber);
        TauLogger.report("Закрыта вкладка %d", tabNumber);
    }

    @Step("Закрыть браузер")
    protected void closeBrowser() {
        run(driver::closeBrowser, "Не удалось закрыть браузер");
        TauLogger.console("Браузер закрыт");
    }
}
