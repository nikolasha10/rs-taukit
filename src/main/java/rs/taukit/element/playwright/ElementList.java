package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

public class ElementList extends PlaywrightElement implements IHasEditableValue {
    protected final Locator buttons;
    protected final Locator labels;

    public ElementList(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName, elementData);
        this.labels = driver.getLocator(elementData.get("Названия"));
        this.buttons = driver.getLocator(elementData.get("Кнопки"));
    }
    protected void setValueAction(String value){
        element.locator(labels)
                .filter(new Locator.FilterOptions().setHasText(value))
                .filter(new Locator.FilterOptions().setHas(buttons))
                .click();
    }
    @Override
    @Step("в списке '{this.elementName}' выбрать значение '{value}'")
    public void setValue(String value) {
        try {
            setValueAction(value);
            TauLogger.report("В списке '%s' выбрано значение '%s'", elementName, value);
        } catch (Exception e){
            TauLogger.fail(e,"Не удалось выбрать значение '%s' в списке '%s'", value, elementName);
        }
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public boolean isVisible() {
        return element.isVisible();
    }

    @Override
    public void click() {

    }
}
