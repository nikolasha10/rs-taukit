package rs.taukit.step.manager;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.CanBeChecked;
import rs.taukit.utils.TauLogger;

public class CheckboxManager extends StepManager {

    @Step("установить чекбокс '{elementName}'")
    public void check(String elementName) {
        run(() -> {
                    getPage().getElement(elementName).castTo(CanBeChecked.class).setCheckbox();
                    TauLogger.report("Установлен чекбокс '%s'", elementName);
                },
                "Не удалось установить чекбокс '%s' на странице '%s'", elementName, getPage().getPageName());
    }

    @Step("снять чекбокс '{elementName}'")
    public void uncheck(String elementName) {
        run(() -> {
                    getPage().getElement(elementName).castTo(CanBeChecked.class).unsetCheckbox();
                    TauLogger.report("Снят чекбокс '%s'", elementName);
                },
                "Не удалось снять чекбокс '%s' на странице '%s'", elementName, getPage().getPageName());
    }
}
