package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasMenu;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.List;

public class PwMenu extends PwElement implements IHasMenu {
    protected Locator items;
    protected Locator expand;
    protected Locator dropdownListItems;

    public PwMenu(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwMenu(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwMenu(String elementName, ElementModel model) {
        super(elementName, model);
        initFields(model);
    }

    public PwMenu(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        initFields(model);
    }

    private void initFields(ElementModel model) {
        this.items = getPwLocator(model.get("пункты меню"));
        String menuExpandButtonLocator = model.get("...", false);
        if (menuExpandButtonLocator != null) {
            this.expand = getPwLocator(menuExpandButtonLocator);
            this.dropdownListItems = getPwLocator(model.get("выпадающий список"));
        }
    }


    @Override
    public void selectMenuItem(String itemName) {
        Locator item = element.locator(items).filter(new Locator.FilterOptions().setHasText(itemName));
        int itemCount = item.count();
        int dropdownItemCount = 0;
        if (itemCount == 0) {
            List<String> dropdownItemList = element.locator(items).allTextContents();
            if (expand != null) {
                element.locator(expand).click();
                dropdownItemCount = element.locator(dropdownListItems)
                        .filter(new Locator.FilterOptions().setHasText(itemName)).count();
            }
            if (dropdownItemCount == 0) {
                dropdownItemList.addAll(element.locator(dropdownListItems).allTextContents());
                TauLogger.fail("Не удалось выбрать \"%s\" в меню \"%s\". Доступны значения: %s",
                        itemName, elementName, dropdownItemList);
            }
            element.locator(dropdownListItems)
                    .filter(new Locator.FilterOptions().setHasText(itemName))
                    .click();
//            testContext().setStepAction(() -> {
//                element.locator(dropdownListItems)
//                        .filter(new Locator.FilterOptions().setHasText(itemName))
//                        .click();
//                return true;
//            });
        } else {
            item.click();
//            testContext().setStepAction(() -> {
//                item.click();
//                return true;
//            });
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
            TauLogger.report("В списке \"%s\" выбрано значение \"%s\"", elementName, value);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось выбрать значение \"%s\" в списке \"%s\"", value, elementName);
        }
    }

}
