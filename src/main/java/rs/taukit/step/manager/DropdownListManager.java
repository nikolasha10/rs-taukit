package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.element.interfaces.Selectable;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class DropdownListManager extends StepManager {

    @Step("в списке '{elementName}' выбрать значение '{value}'")
    public void selectValue(String elementName, String value) {
        run(() -> getPage().getElement(elementName).castTo(Editable.class).setValue(value),
                "Не удалось выбрать значение '%s' в выпадающем списке '%s'", value, elementName);
        TauLogger.console("В выпадающем списке '%s' выбрано значение '%s'", elementName, value);
    }

    @Step("в выпадающем списке '{elementName}' проверить отображаемые значения")
    public void verifyDropdownOptions(String elementName, DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемые значения в списке", dataTable);
        List<String> expectedValues = dataTable.asList();
        run(() -> {
            Selectable dropdown = getElement(elementName);
            List<String> actualValues = dropdown.getOptions();
            if (expectedValues.size() != actualValues.size()
                    || !new HashSet<>(expectedValues).containsAll(actualValues)
                    || !new HashSet<>(actualValues).containsAll(expectedValues)) {
                List<List<String>> diff = new ArrayList<>();
                diff.add(Arrays.asList("Ожидаемые значения", "Фактические значения"));
                for (int i = 0; i < Math.max(actualValues.size(), expectedValues.size()); i++) {
                    diff.add(Arrays.asList(
                            i < expectedValues.size() ? expectedValues.toArray()[i].toString() : "",
                            i < actualValues.size() ? actualValues.toArray()[i].toString() : ""));
                }
                DataTable diffTable = DataTable.create(diff, TextUtils.tableConverter);
                TauLogger.attachDataTable("Разница в значениях", diffTable);
                TauLogger.console("Разница в значениях", diffTable);
                throw new RuntimeException();
            }
        }, "Значения выпадающего списка '%s' не совпадают с ожидаемыми", elementName);
        TauLogger.console("Выпадающий список '%s' содержит ожидаемые значения: %s", elementName, expectedValues);
    }

    @Step("в выпадающем списке '{elementName}' выбрать {number}-й элемент")
    protected void selectElementByNumber(String elementName, int number) {
        AtomicReference<RuntimeException> exception = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            try {
                Selectable dropdown = getElement(elementName);
                dropdown.getOptionByNumber(number).click();
                TauLogger.console("В выпадающем списке '%s' выбран %d-й элемент", elementName, number);
                return true;
            } catch (RuntimeException e) {
                exception.set(e);
                return false;
            }
        };
        waitForCondition(condition, exception.get(),
                "Не удалось выбрать %d-й элемент в выпадающем списке '%s'", number, elementName);
    }
}
