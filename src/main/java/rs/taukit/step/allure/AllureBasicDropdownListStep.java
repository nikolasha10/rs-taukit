package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.step.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicDropdownListStep extends BasicUtils {
    private final String fieldName;
    private final IHasEditableValue dropdownList;

    public AllureBasicDropdownListStep(String fieldName) {
        this.fieldName = fieldName;
        Class<IElement> elementType = ElementType.getType("выпадающий список").getElementClass();
        this.dropdownList = page().replaceElement(fieldName, elementType).castTo(IHasEditableValue.class);
    }

    @Step("в списке '{this.fieldName}' выбрать значение '{value}'")
    public void selectValue(String value) {
        try {
            dropdownList.setValue(value);
            TauLogger.report("В выпадающем списке '%s' выбрано значение '%s'", fieldName, value);
        } catch (Exception e){
            TauLogger.fail(e,"Не удалось выбрать значение '%s' в выпадающем списке '%s'", value, fieldName);
        }
    }
}
