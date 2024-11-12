package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.element.interfaces.IHasListItems;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class ListManager extends StepManager {

    @Step("в списке '{listName}' выбрать '{listValue}'")
    public void listValueClick(String listName, String listValue) {
        run(() -> {
            IHasEditableValue list = getElement(listName);
            list.setValue(listValue);
        }, "Не удалось выбрать значение '%s' в списке '%s' на странице '%s'", 
                listValue, listName, getPage().getPageName());
        TauLogger.report("в списке '%s' выбран '%s'", listName, listValue);
    }

    @Step("в списке '{listName}' найти {itemType}, соответствующий условиям:")
    public void findItemByConditions(String listName, String itemType, DataTable dataTable) {
        TauLogger.attachDataTable("Условия поиска элемента", dataTable);
        AtomicReference<IContainer> foundItem = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            IHasListItems list = getElement(listName);
            foundItem.set(list.findItemByConditions(dataTable));
            return foundItem.get() != null;
        };
        waitForCondition(condition,
                "Не удалось найти %s, соответствующий заданным условиям, в списке '%s' на странице '%s'",
                itemType, listName, getPage().getPageName());
        int foundItemNumber = foundItem.get().getCurrentIndex() + 1;
        TauLogger.report("Найден %s № %d", itemType, foundItemNumber);
        getPage().setCurrentContext(foundItem.get());
        scenarioStorage.clearStepActions();
        scenarioStorage.stopToSaveActions();
        TauLogger.resetNotCritical();
        TauLogger.report("[примечание] текущий контекст: %d-й элемент списка '%s'", foundItemNumber, listName);
    }

    @Step("проверить состав элементов списка:")
    public void validateElements(String listName, DataTable dataTable) {
        TauLogger.attachDataTable("Элементы списка", dataTable);
        IHasListItems list = getElement(listName);
        Map<String, String> expected = new HashMap<>();
        Map<String, IElement> items = list.getItemList().stream()
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

        if (resultList.size() == 1) {
            TauLogger.console("Состав элементов списка '%s' соответствует ожидаемому", listName);
        } else {
            DataTable validationResultTable = DataTable.create(resultList, TextUtils.tableConverter);
            System.out.println(validationResultTable);
            Runnable runnable = () -> TauLogger.attach("Элементы списка, не прошедшие проверку", validationResultTable);
            TauLogger.fail(runnable, "Состав элементов списка '%s' не соответствует ожидаемому", listName);
        }
    }
}
