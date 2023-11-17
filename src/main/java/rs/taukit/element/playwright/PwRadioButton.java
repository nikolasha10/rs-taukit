package rs.taukit.element.playwright;

import io.qameta.allure.Step;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

public class PwRadioButton extends PwList implements IHasEditableValue {
    public PwRadioButton(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }
    public PwRadioButton(String elementName, ElementModel model) {
        super(elementName, model);
    }
    public PwRadioButton(String elementName, IElement element) {
        super(elementName, element);
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
