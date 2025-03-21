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
        run(() -> getPage().waitForPopup(pageName),
                "Не удалось дождаться отображения всплывающего окна '%s'", pageName);
        TauLogger.report("Отображается всплывающее окно '%s'", pageName);
    }

    @Step("дождаться закрытия всплывающего окна '{pageName}'")
    public void waitForPopupClose(String pageName) {
        run(() -> getPage().waitForPopupClose(pageName),
                "Не удалось дождаться закрытия всплывающего окна '%s'", pageName);
        TauLogger.report("Закрыто всплывающее окно '%s'", pageName);
        TauLogger.report("[примечание] открыта страница: %s", getPage().getPageName());
        TauLogger.report("[примечание] текущий контекст: %s '%s'",
                getPage().getCurrentContext().getModel().getType(), getPage().getCurrentContext().getElementName());
    }

    @Step("сохранить текущий url, как '{parameterName}'")
    protected void saveCurrentUrl(String parameterName) {
        scenarioData.put(parameterName, getPage().getCurrentUrl());
        TauLogger.report("%s: %s", parameterName, scenarioData.get(parameterName));
    }
}
