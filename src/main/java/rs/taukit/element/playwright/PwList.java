package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.cucumber.datatable.DataTable;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.*;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static rs.taukit.utils.BasicUtils.page;

public class PwList extends PwElement implements IHasListItems {
    public PwList(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwList(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwList(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwList(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    @Override
    public List<PwBlock> getItemList() {
        return elementPwLocator.all().stream().map(locator -> {
            PwBlock container = new PwBlock(elementName, model);
            return ElementBuilder.buildElement(elementName, container, locator).castTo(PwBlock.class);
        }).collect(Collectors.toList());
    }

    @Override
    public void setValue(String value) {
        double timeout = Config.getTimeProperty("element.check.interval");
        BooleanSupplier condition = () -> getValues().stream().anyMatch(itemValue -> itemValue.contains(value));
        try {
            driver.waitForCondition(condition, timeout);
        } catch (Exception e) {
            throw valueNotFoundException(value).get();
        }
        getItemList().stream()
                .filter(PwElement::isVisible)
                .filter(containsElementWithText(value))
                .findFirst().orElseThrow(valueNotFoundException(value))
                .click();
    }

    private Predicate<IContainer> containsElementWithText(String text) {
        return item -> item.containsText(text) || item.getElementByText(text) != null;
    }

    public boolean contains(String value) {
        return getItemList().stream().anyMatch(containsElementWithText(value));
    }

    public List<String> getValues() {
        AtomicReference<List<String>> values = new AtomicReference<>();
        AtomicReference<Exception> ex = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            try {
                List<String> itemsText = getItemList().stream()
                        .filter(CanBeVisible::isVisible)
                        .map(HasText::getText)
                        .toList();
                values.set(itemsText);
                return true;
            } catch (Exception e) {
                TauLogger.console("values: %s", values.get());
                TauLogger.console(e);
                ex.set(e);
                return false;
            }
        };
        try {
            driver.waitForCondition(condition, Config.getTimeProperty("element.check.timeout"));
        } catch (Exception e) {
            throw valuesNotFoundException(ex.get()).get();
        }
        return values.get();
    }

    public PwElement get(int itemIndex) {
        return getItemList().get(itemIndex);
    }

    @Override
    public PwBlock findItemByConditions(DataTable dataTable) {
        PwBlock foundItem = null;
        IContainer originalContext = page().getCurrentContext();
        int foundIndex = IntStream.range(0, getItemList().size()).boxed()
                .filter(index -> dataTable.asMaps().stream().allMatch(data -> {
                    IContainer item = getItemList().get(index);
                    page().setCurrentContext(item);
                    return new TestCondition(data).isConditionMet();
                })).findFirst().orElse(-1);
        page().setCurrentContext(originalContext);
        if (foundIndex > -1) {
            foundItem = getItemList().get(foundIndex);
            foundItem.setCurrentIndex(foundIndex);
        }
        return foundItem;
    }

    private Supplier<RuntimeException> valueNotFoundException(String value) {
        return () -> new RuntimeException(String.format("""
                        Не удалось найти значение '%s' в списке '%s' на странице '%s'.
                        Доступны значения: %s""",
                value, elementName, page().getPageName(), getValues()));
    }

    private Supplier<AutoTestBrokenException> valuesNotFoundException(Exception e) {
        return () -> new AutoTestBrokenException(e, "Не удалось получить значения списка '%s' на странице '%s'",
                elementName, page().getPageName());
    }

    private Supplier<AutoTestBrokenException> itemNotFoundException(String itemType, Throwable cause) {
        return () -> new AutoTestBrokenException(cause,
                "Не удалось найти %s, соответствующий заданным условиям, в списке '%s' на странице '%s'",
                itemType, elementName, page().getPageName());
    }

}
