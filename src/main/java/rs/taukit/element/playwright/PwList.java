package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;

import java.util.LinkedHashMap;

public class PwList extends PwElement implements IHasEditableValue {
    protected final Locator buttons;
    protected final Locator labels;

    public PwList(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwList(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        this.labels = getPwLocator(model.getOrDefault("названия", "//li//button/div"));
        this.buttons = getPwLocator(model.getOrDefault("кнопки", "//parent::button"));
    }

    public PwList(String elementName, ElementModel model) {
        super(elementName, model);
        this.labels = getPwLocator(model.getOrDefault("названия", "//li//button/div"));
        this.buttons = getPwLocator(model.getOrDefault("кнопки", "//parent::button"));
    }

    public PwList(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    protected void setValueAction(String value) {
        element.locator(labels).filter(new Locator.FilterOptions().setHasText(value)).filter(new Locator.FilterOptions().setHas(buttons)).click();
    }

    @Override
    public void setValue(String value) {
        setValueAction(value);
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public void click() {

    }
}
