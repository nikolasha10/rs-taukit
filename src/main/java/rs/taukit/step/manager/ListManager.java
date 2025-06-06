package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.HasListItems;
import rs.taukit.element.interfaces.HasText;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class ListManager extends StepManager {

    @Step("в списке '{listName}' выбрать '{itemName}'")
    public void selectItem(String listName, String itemName) {
        run(() -> {
                    HasListItems list = getElement(listName);
                    list.selectItem(itemName);
                }, "Не удалось выбрать значение '%s' в списке '%s' на странице '%s'",
                itemName, listName, getPage().getPageName());
        TauLogger.report("в списке '%s' выбран '%s'", listName, itemName);
    }

    @Step("в списке '{listName}' найти {itemType}, соответствующий условиям:")
    public void findItemByConditions(String listName, String itemType, DataTable dataTable) {
        TauLogger.attachDataTable("Условия поиска элемента", dataTable);
        AtomicReference<IContainer> foundItem = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            HasListItems list = getElement(listName);
            foundItem.set(list.findItemByConditions(dataTable));
            return foundItem.get() != null;
        };
        waitForCondition(condition,
                "Не удалось найти %s, соответствующий заданным условиям, в списке '%s' на странице '%s'",
                itemType, listName, getPage().getPageName());
        int foundItemNumber = 1;
        if (foundItem.get() != null) {
            foundItemNumber = foundItem.get().getCurrentIndex() + 1;
            TauLogger.report("Найден %s № %d", itemType, foundItemNumber);
        } else {
            foundItem.set(((HasListItems) getElement(listName)).getItemList().getFirst().castTo(IContainer.class));
        }
        getPage().setCurrentContext(foundItem.get());
        scenarioStorage.setCurrentContextName(foundItem.get().getElementName());

        scenarioStorage.clearStepActions();
        scenarioStorage.stopToSaveActions();
        TauLogger.resetNotCritical();
        TauLogger.report("[примечание] текущий контекст: %d-й элемент списка '%s'", foundItemNumber, listName);
    }

    @Step("проверить состав элементов списка '{listName}':")
    public void validateElements(String listName, DataTable dataTable) {
        TauLogger.attachDataTable("Элементы списка", dataTable);
        HasListItems list = getElement(listName);
        Map<String, String> expected = new HashMap<>();
        List<List<String>> resultList = new ArrayList<>();
        dataTable.asMaps().forEach(row -> {
            String elementText = TestDataUtils.get(row, "Название");
            String elementState = TestDataUtils.get(row, "Состояние");
            expected.put(elementText, elementState);
        });
        BooleanSupplier condition = () -> {
            Map<String, IElement> items = list.getItemList().stream()
                    .collect(Collectors.toMap(item -> item.castTo(HasText.class).getText(), item -> item));
            resultList.clear();
            expected.forEach((itemName, expectedState) -> {
                Optional<Map.Entry<String, IElement>> optionalItem = items.entrySet().stream()
                        .filter(entry -> entry.getKey().contains(itemName)).findFirst();
                AtomicReference<String> actualState = new AtomicReference<>("не отображается");
                optionalItem.ifPresent(entry -> actualState.set(entry.getValue().isVisible() ? "отображается" : "не отображается"));
                if (!expectedState.equals(actualState.get())) {
                    resultList.add(Arrays.asList(itemName, actualState.get(), expectedState));
                }
            });
            return resultList.isEmpty();
        };
        try {
            getDriver().waitForCondition(condition);
            TauLogger.console("Состав элементов списка '%s' соответствует ожидаемому", listName);
        } catch (Exception e) {
            resultList.addFirst(Arrays.asList("Элемент", "Состояние", "Ожидалось"));
            DataTable validationResultTable = DataTable.create(resultList, TextUtils.tableConverter);
            System.out.println(validationResultTable);
            Runnable runnable = () -> TauLogger.attach("Элементы списка, не прошедшие проверку", validationResultTable);
            TauLogger.fail(runnable, "Состав элементов списка '%s' не соответствует ожидаемому", listName);
        }
    }

    @Step("проверить, что все блочные элементы списка '{listName}' соответствуют условиям:")
    protected void validateAllBlockElements(String listName, DataTable dataTable) {
        TauLogger.attachDataTable("Условия валидации элементов списка", dataTable);
        List<List<String>> resultList = new ArrayList<>();
        resultList.add(Arrays.asList("№", "Объект", "Название", "Состояние", "текущее значение", "Ожидалось"));
        HasListItems list = getElement(listName);
        for (int i = 0; i < list.getSize(); i++) {
            IContainer item = list.getItemList().get(i).castTo(IContainer.class);
            getPage().setCurrentContext(item);
            int itemNumber = i + 1;
            dataTable.asMaps().forEach(conditionData -> {
                TestCondition testCondition = new TestCondition(conditionData).checkResult();
                if (!testCondition.isConditionMet()) {
                    resultList.add(Arrays.asList(
                            String.valueOf(itemNumber),
                            testCondition.getObjectType(),
                            testCondition.getObjectName(),
                            testCondition.getObjectState(),
                            testCondition.getCurrentValue(),
                            testCondition.getExpectedValue()));
                }
            });
            getPage().dropCurrentContext();
        }
        if (resultList.size() > 1) {
            Runnable runnable = () -> {
                DataTable resultDataTable = DataTable.create(resultList, TextUtils.tableConverter);
                TauLogger.attachDataTable("Список элементов, не прошедших валидацию", resultDataTable);
            };
            TauLogger.fail(runnable, "Элементы списка '%s' не соответствуют требуемым условиям", listName);
        }
        TauLogger.resetNotCritical();
        TauLogger.console("Все блочные элементы списка '%s' соответствуют требуемым условиям", listName);
    }

    @Step("проверить, что список '{listName}' содержит ровно {count} {word}")
    protected void validateListElementsCountEquals(String listName, String count, String word) {
        HasListItems list = getElement(listName);
        if (list.getSize() != Integer.parseInt(count)) {
            TauLogger.fail("Количество элементов списка '%s' (%d) не равно ожидаемому (%s)", listName, list.getSize(), count);
        } else {
            TauLogger.console("Количество элементов списка '%s' равно ожидаемому: %s", listName, count);
        }
    }
}
