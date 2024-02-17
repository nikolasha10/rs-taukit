package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasMenu;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

public class PwMenu extends PwElement implements IHasMenu {
    public PwMenu(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwMenu(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwMenu(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwMenu(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    @Override
    public void selectMenuItem(String itemName) {
        PwList itemList = getDescendant("Пункты меню");
        if (itemList.getItemList().stream().anyMatch(item -> item.getText().equals(itemName))) {
            itemList.setValue(itemName);
        } else {
            PwButton item = getDescendant("Дополнительные пункты");
            item.click();
            PwList additionalItemList = getDescendant("Выпадающий список");
            additionalItemList.setValue(itemName);
        }
    }

    @Override
    @Step("в списке '{this.elementName}' выбрать значение '{value}'")
    public void setValue(String value) {
        try {
            selectMenuItem(value);
            TauLogger.report("В списке \"%s\" выбрано значение \"%s\"", elementName, value);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось выбрать значение \"%s\" в списке \"%s\"", value, elementName);
        }
    }

}
