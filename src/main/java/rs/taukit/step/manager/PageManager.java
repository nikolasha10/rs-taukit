package rs.taukit.step.manager;

import io.qameta.allure.Step;
import rs.taukit.utils.TauLogger;

public class PageManager extends StepManager {
    @Step("открыть страницу '{pageName}'")
    public void openPage(String pageName) {
        run(() -> getBrowser().getPage(pageName).loadPage());
        TauLogger.report("Открыта страница '%s'", pageName);
    }

    @Step("дождаться отображения страницы '{pageName}'")
    public void waitForPageLoad(String pageName) {
        run(() -> getBrowser().getPage(pageName).waitForLoad());
        TauLogger.report("Отображается страница '%s'", pageName);
    }


    @Step("дождаться отображения всплывающего окна '{pageName}'")
    public void waitForPopupWindow(String pageName) {
        run(() -> {
            scenarioStorage.setSavedPageName(getPage().getPageName());
            getBrowser().getPage(pageName).waitForLoad();
        });
        TauLogger.report("Отображается всплывающее окно '%s'", pageName);
    }

    @Step("дождаться закрытия всплывающего окна '{pageName}'")
    public void waitForPopupClose(String pageName) {
        run(() -> {
            getBrowser().getPage(pageName).waitForClose();
            getBrowser().getPage(scenarioStorage.getSavedPageName());
        });
        TauLogger.report("Закрыто всплывающее окно '%s'", pageName);
    }
}
