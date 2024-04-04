package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.Element;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.element.interfaces.IHasListItems;
import rs.taukit.exceptions.ElementListException;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class AllureBasicListStep extends BasicUtils {
    private final String listName;
    private final Element list;

    public AllureBasicListStep(String listName) {
        this.listName = listName;
        this.list = page().getElement(listName);
    }

    @Step("в списке '{this.listName}' выбрать '{listValue}'")
    public void listValueClick(String listValue) {
        Runnable runnable = () -> list.castTo(IHasEditableValue.class).setValue(listValue);
        scenarioStorage().saveStepAction(runnable);
        runnable.run();
        TauLogger.report("в списке \"%s\" выбран \"%s\"", listName, listValue);
    }

    @Step("в списке \"{this.listName}\" найти {itemType}, соответствующий условиям:")
    public void findItemByConditions(String itemType, DataTable dataTable) {
        TauLogger.attachDataTable("Условия поиска элемента", dataTable);
        AtomicReference<IHasBlockElements> foundItem = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            foundItem.set(list.castTo(IHasListItems.class).findItemByConditions(dataTable));
            return foundItem.get() != null;
        };
        try {
            driver().waitForCondition(condition);
        } catch (Exception e) {
            throw ElementListException.itemNotFound(listName, itemType, e).get();
        }
        int foundItemNumber = foundItem.get().getCurrentIndex() + 1;
        TauLogger.report("Найден %s № %d", itemType, foundItemNumber);
        page().setCurrentContext(foundItem.get());
        TauLogger.report("[примечание] текущий контекст: %d-й элемент списка '%s'", foundItemNumber, listName);
    }

    @Step("проверить состав элементов списка:")
    public void validateElements(DataTable dataTable) {
        TauLogger.attachDataTable("Элементы списка", dataTable);
        Map<String, String> expected = new HashMap<>();
        Map<String, IElement> items = list.castTo(IHasListItems.class).getItemList().stream()
                .collect(Collectors.toMap(IElement::getText, item -> item));
        List<List<String>> resultList = new ArrayList<>();
        resultList.add(Arrays.asList("Элемент", "Состояние", "Ожидалось"));
        dataTable.asMaps().forEach(row -> {
            String elementText = TestDataUtils.get(row, "Название");
            String elementState = TestDataUtils.get(row, "Состояние");
            expected.put(elementText, elementState);
        });
        expected.forEach((itemName, expectedState) -> {
            String actualState = items.containsKey(itemName) && items.get(itemName).isVisible() ? "отображается" : "не отображается";
            if (!expectedState.equals(actualState)) {
                resultList.add(Arrays.asList(itemName, actualState, expectedState));
            }
        });

        if (resultList.isEmpty()) {
            TauLogger.console("Состав элементов списка '%s' соответствует ожидаемому", this.listName);
        } else {
            DataTable validationResultTable = DataTable.create(resultList, TextUtils.tableConverter);
            System.out.println(validationResultTable);
            Runnable runnable = () -> TauLogger.attach("Элементы списка, не прошедшие проверку", validationResultTable);
            TauLogger.fail(runnable, "Состав элементов списка '%s' не соответствует ожидаемому", this.listName);
        }
    }
}
