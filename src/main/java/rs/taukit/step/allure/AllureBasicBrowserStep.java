package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.page.PageObject;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicBrowserStep extends BasicUtils {
    private final IWebDriver driver;
    private IWebBrowser browser;

    public AllureBasicBrowserStep() {
        this.driver = driver();
        this.browser = this.driver.isBrowserOpen() ? this.driver.getCurrentBrowser() : null;
    }

    @Step("открыть браузер")
    public void openBrowser() {
        if (!driver.isBrowserOpen()) {
            driver.openBrowser();
        }
        TauLogger.report("Открыт браузер \"%s\"", driver.getBrowserType());
    }

    @Step("открыть ещё один браузер")
    public void openAnotherOneBrowser() {
        driver.openBrowser();
        TauLogger.report("Открыт браузер \"%s\"", driver.getBrowserType());
    }

    @Step("обновить страницу")
    public void reloadPage() {
        page().reload();
        testContext().saveStepAction(() -> page().reload());
        TauLogger.report("Страница \"%s\" обновлена", testContext().getCurrentPageName());
    }

    @Step("закрыть текущую вкладку")
    public void closeCurrentTab() {
        int tabIndex = browser.getActiveTabIndex();
        browser.closeCurrentTab();
        TauLogger.report("Закрыта вкладка %d", tabIndex + 1);
        TauLogger.report("[примечание] открыта вкладка %d", browser.getActiveTabIndex() + 1);
        TauLogger.report("[примечание] открыта страница: %s", page().getPageName());
        IHasBlockElements currentContext = page().getCurrentContext();
        if (currentContext != null) {
            TauLogger.report("[примечание] текущий контекст: %s %s", currentContext.getModel().getType(), currentContext.getElementName());
        }
    }


    public void continueWithBrowser() {
        continueWithBrowser(driver.getCurrentBrowserIndex());
    }

    @Step("продолжить работу в открытом ранее браузере ({browserIndex})")
    public void continueWithBrowser(int browserIndex) {
        driver.switchBrowser(browserIndex - 1);
        browser = driver.getCurrentBrowser();
        browser.setActiveTab(browser.getActiveTabIndex());
        PageObject currentPage = browser.getCurrentPage();
        IHasBlockElements currentContext = currentPage.getCurrentContext();
        String contextType;
        String contextName;
        if (currentContext == null) {
            contextType = "страница";
            contextName = currentPage.getPageName();
        } else {
            contextType = currentContext.getModel().getType();
            contextName = currentContext.getElementName();
        }
        testContext().setCurrentContextName("%s \"%s\"", contextType, contextName);
        TauLogger.report("Активен браузер: %d", driver.getCurrentBrowserIndex() + 1);
        TauLogger.report("[примечание] открыта вкладка: %d", browser.getActiveTabIndex() + 1);
        TauLogger.report("[примечание] открыта страница: %s", currentPage.getPageName());
        TauLogger.report("[примечание] текущий контекст: %s \"%s\"", contextType, contextName);
    }

    @Step("дождаться отображения страницы \"{pageName}\" в новой вкладке")
    public void waitForPageLoadNewTab(String pageName) {
        IWebBrowser browser = driver().getCurrentBrowser();
        int activeTabIndex = browser.getActiveTabIndex();
        try {
            driver().waitForCondition(() -> browser.setActiveTab(activeTabIndex + 1));
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться открытия новой вкладки браузера");
        }
        TauLogger.report("Открыта вкладка %d", browser.getActiveTabIndex() + 1);
        browser.getPage(pageName).waitForLoad();
        TauLogger.report("Отображается страница \"%s\"", pageName);

    }

    @Step("перейти по ссылке {url}")
    public void navigate(String url) {
        try {
            browser.navigate(url);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось перейти по ссылке \"%s\"", url);
        }
    }

    @Step("продолжить работу в браузере № {browserNumber}")
    public void continueWithBrowserNumber(int browserNumber) {
        driver.switchBrowser(browserNumber - 1);
        TauLogger.report("Активен браузер %d", browserNumber);
        TauLogger.report("[примечание] открыта вкладка %d", browser().getActiveTabIndex() + 1);
        TauLogger.report("[примечание] отображается страница: %s", browser().getCurrentPage().getPageName());
        IHasBlockElements currentContext = browser().getCurrentPage().getCurrentContext();
        if (currentContext != null) {
            TauLogger.report("[примечание] текущий контекст: %s '%s'",
                    currentContext.getModel().getType(), currentContext.getElementName());
        }
    }

    public void closeTab(int tabNumber) {
        browser.closeTab(tabNumber - 1);
        TauLogger.report("Закрыта вкладка %d", tabNumber);
    }
}
