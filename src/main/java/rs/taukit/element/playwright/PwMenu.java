package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasMenu;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.List;

public class PwMenu extends PwElement implements IHasMenu {
    protected final Locator items;
    protected final Locator expand;
    protected final Locator dropdownListItems;

    public PwMenu(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
        this.items = driver.getLocator(model.get("пункты меню"));
        this.expand = driver.getLocator(model.get("..."));
        this.dropdownListItems = driver.getLocator(model.get("выпадающий список"));
    }

    public PwMenu(String elementName, ElementModel model) {
        super(elementName, model);
        this.items = driver.getLocator(model.get("пункты меню"));
        this.expand = driver.getLocator(model.get("..."));
        this.dropdownListItems = driver.getLocator(model.get("выпадающий список"));
    }

    @Override
    public void selectMenuItem(String itemName) {
        int itemCount = element.locator(items)
                .filter(new Locator.FilterOptions().setHasText(itemName)).count();
        if (itemCount == 0){
            element.locator(expand).click();
            int dropdownItemCount = element.locator(dropdownListItems)
                    .filter(new Locator.FilterOptions().setHasText(itemName)).count();
            if (dropdownItemCount == 0){
                List<String> dropdownItemList = element.locator(dropdownListItems).allTextContents();
                TauLogger.fail("Не удалось выбрать '%s' в меню '%s'. Доступны значения: %s",
                        itemName, elementName, dropdownItemList);
            }
            element.locator(dropdownListItems)
                    .filter(new Locator.FilterOptions().setHasText(itemName))
                    .click();
        }
    }

    protected void setValueAction(String value) {
        element.locator(items)
                .filter(new Locator.FilterOptions().setHasText(value))
                .filter(new Locator.FilterOptions().setHas(dropdownListItems))
                .click();
    }

    @Override
    @Step("в списке '{this.elementName}' выбрать значение '{value}'")
    public void setValue(String value) {
        try {
            setValueAction(value);
            TauLogger.report("В списке '%s' выбрано значение '%s'", elementName, value);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось выбрать значение '%s' в списке '%s'", value, elementName);
        }
    }

}
