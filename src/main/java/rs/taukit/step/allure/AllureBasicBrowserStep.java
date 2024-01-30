package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.driver.IWebBrowser;
import rs.taukit.driver.IWebDriver;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicBrowserStep extends BasicUtils {
    private final IWebDriver driver;
    private IWebBrowser browser;

    public AllureBasicBrowserStep() {
        this.driver = driver();
        this.browser = this.driver.getCurrentBrowser();
    }

    @Step("открыть браузер")
    public void openBrowser() {
        if (!driver.isBrowserOpen()) {
            driver.openBrowser();
        }
        TauLogger.report("Открыт браузер '%s'", driver.getBrowserType());
    }
    @Step("открыть ещё один браузер")
    public void openAnotherOneBrowser() {
        driver.openBrowser();
        TauLogger.report("Открыт браузер '%s'", driver.getBrowserType());
    }

    @Step("обновить страницу")
    public void reloadPage() {
        page().reload();
        testContext().saveStepAction(() -> page().reload());
        TauLogger.report("Страница '%s' обновлена", testContext().getCurrentPageName());
    }

    @Step("закрыть текущую вкладку")
    public void closeCurrentTab() {
        int tabIndex = browser.getActiveTabIndex();
        browser.closeCurrentTab();
        TauLogger.report("Вкладка %d закрыта", tabIndex + 1);
    }


    public void continueWithBrowser() {
        continueWithBrowser(driver.getCurrentBrowserIndex() - 1);
    }
    @Step("продолжить работу в открытом ранее браузере ({browserIndex})")
    public void continueWithBrowser(int browserIndex) {
        driver.switchBrowser(driver.getCurrentBrowserIndex() - 1);
        browser = driver.getCurrentBrowser();
        TauLogger.report("[примечание] Активен браузер: %d", browserIndex + 1);
        TauLogger.report("[примечание] Открыта вкладка: %d", browser.getActiveTabIndex() + 1);
        TauLogger.report("[примечание] Открыта страница: %s", browser.getCurrentPage().getPageName());
    }

    @Step("перейти по ссылке {url}")
    public void navigate(String url) {
        try {
            browser.navigate(url);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось перейти по ссылке '%s'", url);
        }
    }

}
