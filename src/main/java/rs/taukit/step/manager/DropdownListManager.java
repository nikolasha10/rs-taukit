package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.utils.TauLogger;
import java.util.ArrayList;
import java.util.List;

public class DropdownListManager extends StepManager {

    @Step("в списке '{elementName}' выбрать значение '{value}'")
    public void selectValue(String elementName, String value) {
        run(() -> getPage().getElement(elementName).castTo(Editable.class).setValue(value),
                "Не удалось выбрать значение '%s' в выпадающем списке '%s'", value, elementName);
        TauLogger.report("В выпадающем списке '%s' выбрано значение '%s'", elementName, value);
    }

    @Step("в выпадающем списке '{elementName}' проверить отображаемые значения")
    public void verifyDropdownOptions(String elementName, DataTable dataTable) {
        List<String> expectedValues = new ArrayList<>();
        for (List<String> row : dataTable.asLists(String.class)) {
            String cell = row.get(0);
            expectedValues.add(cell != null ? cell.trim() : "");
        }
        TauLogger.attachDataTable("Ожидаемые значения в списке", dataTable);
        TauLogger.console("Проверка, что в выпадающем списке '%s' отображаются значения: %s", elementName, expectedValues);
        run(() -> {
            Editable dropdown = getPage().getElement(elementName).castTo(Editable.class);
            List<String> actualValues = dropdown.getOptions();
            if (actualValues == null || actualValues.isEmpty()) {
                throw new AssertionError(
                        "Не удалось получить значения из выпадающего списка '" + elementName + "'");
            }
            List<String> normalizedActual = actualValues.stream()
                    .map(s -> s != null ? s.trim() : "")
                    .toList();
            if (!normalizedActual.containsAll(expectedValues) || !expectedValues.containsAll(normalizedActual)) {
                throw new AssertionError(String.format(
                        "Несоответствие значений в списке '%s'.\nОжидалось: %s\nФактически: %s",
                        elementName, expectedValues, normalizedActual
                ));
            }
            TauLogger.report("В выпадающем списке '%s' подтверждены все ожидаемые значения", elementName);
        }, "Не удалось проверить значения в выпадающем списке '%s'", elementName);
    }
}
