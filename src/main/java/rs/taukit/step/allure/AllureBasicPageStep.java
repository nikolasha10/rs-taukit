package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.page.PageObject;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class AllureBasicPageStep extends BasicUtils {
    private final String pageName;
    private final PageObject page;

    public AllureBasicPageStep(String pageName) {
        this.pageName = pageName;
        this.page = Optional.ofNullable(driver().getCurrentBrowser().getPage(pageName))
                .orElseThrow();
    }

    @Step("открыть страницу \"{this.pageName}\"")
    public void openPage() {
        page.loadPage();
        TauLogger.report("Открыта страница \"%s\"", pageName);
    }

    @Step("дождаться отображения страницы \"{this.pageName}\"")
    public void waitForPageLoad() {
        try {
            page.waitForLoad();
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться отображения страницы \"%s\"", page.getPageName());
        }
        scenarioStorage().saveStepAction(page::waitForLoad);
        TauLogger.report("Отображается страница \"%s\"", pageName);
    }

    @Step("дождаться отображения всплывающего окна \"{this.pageName}\"")
    public void waitForPopupWindow() {
        scenarioStorage().setSavedPageName(scenarioStorage().getCurrentPageName());
        page.waitForLoad();
        TauLogger.report("Отображается всплывающее окно \"%s\"", pageName);
    }

    @Step("дождаться закрытия всплывающего окна \"{this.pageName}\"")
    public void waitForPopupClose() {
        page.waitForClose();
        TauLogger.report("Закрыто всплывающее окно \"%s\"", pageName);
        String currentPageName = scenarioStorage().getSavedPageName();
        BooleanSupplier condition = () -> {
            try {
                driver().getCurrentBrowser().getPage(currentPageName).waitForLoad();
                return true;
            } catch (IllegalStateException ise){
                return false;
            }
        };
        driver().waitForCondition(condition);
        TauLogger.report("Отображается страница \"%s\"", currentPageName);
    }

}
