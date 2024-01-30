package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.page.PageObject;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicPageStep extends BasicUtils {
    private final String pageName;
    private final PageObject page;

    public AllureBasicPageStep(String pageName) {
        this.pageName = pageName;
        this.page = driver().getCurrentBrowser().getPage(pageName);
    }

    @Step("открыть страницу '{this.pageName}'")
    public void openPage() {
        page.loadPage();
        TauLogger.report("Открыта страница '%s'", pageName);
    }

    @Step("дождаться отображения страницы '{this.pageName}'")
    public void waitForPageLoad() {
        page.waitForLoad();
        testContext().saveStepAction(page::waitForLoad);
        TauLogger.report("Отображается страница '%s'", pageName);
    }

    @Step("дождаться отображения всплывающего окна '{this.pageName}'")
    public void waitForPopupWindow() {
        testContext().setSavedPageName(testContext().getCurrentPageName());
        page.waitForLoad();
        TauLogger.report("Отображается всплывающее окно '%s'", pageName);
    }

    @Step("дождаться закрытия всплывающего окна '{this.pageName}'")
    public void waitForPopupClose() {
        page.waitForClose();
        TauLogger.report("Закрыто всплывающее окно '%s'", pageName);
        String currentPageName = testContext().getSavedPageName();
        driver().getCurrentBrowser().getPage(currentPageName).waitForLoad();
        TauLogger.report("Отображается страница '%s'", currentPageName);
    }

}
