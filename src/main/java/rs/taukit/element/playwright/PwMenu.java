package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasMenu;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

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
    public IElement getMenuItem(String itemName) {
        PwList itemList = getDescendant("Пункты меню");
        List<String> itemValues = itemList.getValues();
        AtomicReference<String> errorMessage = new AtomicReference<>("");
        try {
            if (itemList.getItemList().stream().anyMatch(item -> item.getText().equals(itemName))) {
                return itemList.getItem(itemName);
            } else {
                PwButton item = getDescendant("Дополнительные пункты");
                PwList additionalItemList = getDescendant("Выпадающий список");
                getDriver().getCurrentBrowser().setDefaultTimeout(Config.getTimeProperty("element.check.interval"));
                BooleanSupplier condition = () -> {
                    try {
                        if (!additionalItemList.isVisible()) {
                            item.click();
                        }
                        List<String> additionalValues = additionalItemList.getValues();
                        if (!additionalValues.contains(itemName)) {
                            errorMessage.set(String.format("""
                                    . Элемент с текстом '%s' не найден среди пунктов меню.
                                    Доступны основные: %s
                                    Дополнительные: %s""", itemName, itemValues, additionalValues));
                            throw new RuntimeException(errorMessage.get());
                        }
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                };
                getDriver().waitForCondition(condition);
                getDriver().getCurrentBrowser().setDefaultTimeout();
                return additionalItemList.getItem(itemName);
            }
        } catch (Exception e) {
            String message = String.format("""
                    Не удалось найти пункт '%s' в меню '%s'%s""", itemName, elementName, errorMessage.get());
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public void selectMenuItem(String itemName) {
        getDriver().getCurrentBrowser().setDefaultTimeout(Config.getTimeProperty("element.check.interval"));
        AtomicReference<Exception> exception = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            try {
                getMenuItem(itemName).click();
                return true;
            } catch (Exception e) {
                exception.set(e);
                return false;
            }
        };
        try {
            getDriver().waitForCondition(condition);
        } catch (Exception e) {
            throw new RuntimeException(exception.get());
        }
        getDriver().getCurrentBrowser().setDefaultTimeout();
    }

    @Override
    public boolean containsItem(String itemName) {
        PwList itemList = getDescendant("Пункты меню");
        if (itemList.getItemList().stream().anyMatch(item -> item.getText().equals(itemName)))
            return true;
        try {
            PwButton additionalButton = getDescendant("Дополнительные пункты");
            additionalButton.forceClick();
            PwList additionalItemList = getDescendant("Выпадающий список");
            boolean contains = additionalItemList.getValues().stream().anyMatch(itemValue -> itemValue.equals(itemName));
            additionalButton.forceClick();
            return contains;
        } catch (Exception e) {
            TauLogger.console(e);
            return false;
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
