package rs.taukit.element.playwright;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

public class PwRadioButton extends PwList implements IHasEditableValue {
    public PwRadioButton(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName, elementData);
    }

    @Override
    @Step("установить переключатель '{this.elementName}' в значение '{value}'")
    public void setValue(String value) {
        try {
            super.setValueAction(value);
            TauLogger.report("Переключатель '%s' установлен в значение '%s'", elementName, value);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось установить переключатель '%s' в значение '%s'", elementName, value);
        }
    }
}
