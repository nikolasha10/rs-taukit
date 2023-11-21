package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.element.interfaces.IHasCheckbox;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicBlockElementStep extends BasicUtils {
    private final String blockName;
    private final String fieldName;
    private final IHasBlockElements blockElements;
    private final IHasEditableValue field;

    public AllureBasicBlockElementStep(String blockElementName, String fieldName) {
        this.blockName = blockElementName;
        this.fieldName = fieldName;
        Class<IElement> elementType = ElementType.getType("блок элементов").getElementClass();
        String[] elements = blockElementName.split(" => ");
        IHasBlockElements element = page().replaceElement(blockElementName, elementType).castTo(IHasBlockElements.class);
        for (int i = 1; i < elements.length; i++) {
            element = element.getElementByName(1, elements[i]).castTo(IHasBlockElements.class);
        }
        this.blockElements = element;
        this.field = this.blockElements.getElementByName(1, fieldName).castTo(IHasEditableValue.class);
    }

    @Step("в блоке '{this.blockName}' заполнить поле '{this.fieldName}' значением '{value}'")
    public void setValue(String value) {
        field.setValue(value);
        TauLogger.report("Поле '%s' в блоке '%s' заполнено значением '%s'", fieldName, blockName, blockElements.getText());
    }

    @Step("в блоке '{this.blockName}' заполнить поле '{this.fieldName}' значением '{value}'")
    public void setValue(String value, String controlValue) {
        field.setValue(value, controlValue);
        TauLogger.report("Поле '%s' в блоке '%s' заполнено значением '%s'", fieldName, blockName, blockElements.getText());
    }

    @Step("сохранить значения поля '{this.fieldName}' в блоке '{this.blockName}', как '{parameterName}'")
    public void saveElementValue(String parameterName) {
        scenarioData().put(parameterName, field.getText());
    }

    @Step("в блоке '{this.blockName}' нажать на кнопку '{this.fieldName}'")
    public void clickOnField() {
        field.click();
        TauLogger.report("Нажали на поле '%s' в блоке '%s'", fieldName, blockName);
    }

    @Step("в блоке '{this.blockName}' установить чекбокс '{this.fieldName}'")
    public void setCheckbox() {
        field.castTo(IHasCheckbox.class).setCheckbox();
        TauLogger.report("Установлен чекбокс '%s' в блоке '%s'", fieldName, blockName);
    }
}
