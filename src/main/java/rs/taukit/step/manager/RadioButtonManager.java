package rs.taukit.step.manager;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.utils.TauLogger;

public class RadioButtonManager extends StepManager {

    @Step("установить переключатель '{elementName}' в значение '{value}'$")
    public void switchRadio(String elementName, String value) {
        run(() -> {
                    Editable radio = getElement(elementName);
                    radio.setValue(value);
                }, "Не удалось установить переключатель '%s' в значение '%s' на странице '%s'",
                elementName, value, getPage().getPageName());
        TauLogger.report("Переключатель '%s' установлен в значение '%s'", elementName, value);
    }
}
