package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.element.interfaces.HasText;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;

import java.util.List;

public class TextFieldManager extends StepManager {

    @Step("заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String fieldName, String value) {
        run(() -> {
            Editable editableValue = getPage().getElement(fieldName);
            editableValue.setValue(value);
            TauLogger.console("Поле '%s' заполнено значением '%s'", fieldName, value);
        }, "Не удалось заполнить поле '%s' значением '%s'", fieldName, value);
    }

    @Step("заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String fieldName, String value, String controlValue) {
        run(() -> {
            Editable editableValue = getPage().getElement(fieldName);
            editableValue.setValue(value, controlValue);
        }, "Не удалось заполнить поле '%s' значением '%s'", fieldName, value);
        TauLogger.console("Поле '%s' заполнено значением '%s'. Текущее значение совпадает с контрольным (%s)",
                fieldName, value, controlValue);
    }

    @Step("очистить поле '{fieldName}'")
    public void clear(String fieldName) {
        run(() -> {
            Editable editableValue = getPage().getElement(fieldName).castTo(Editable.class);
            editableValue.clear();
        }, "Не удалось очистить поле '%s'", fieldName);
        TauLogger.console("Поле '%s' очищено", fieldName);
    }

    @Step("сохранить значения поля '{fieldName}', как '{parameterName}'")
    public void saveElementValue(String fieldName, String parameterName) {
        run(() -> {
            HasText textField = getPage().getElement(fieldName);
            scenarioData.put(parameterName, textField.getText());
            TauLogger.report("Значение поля '%s' (%s) сохранено, как '%s'",
                    fieldName, scenarioData.get(parameterName), parameterName);
        }, "Не удалось сохранить значение поля '%s' в параметр '%s'", fieldName, parameterName);
    }

    @Step("сохранить значения элементов в параметры:")
    public void saveElementsValue(DataTable dataTable) {
        run(() -> dataTable.asMaps().forEach(data -> {
            String elementName = TestDataUtils.get(data, "Название");
            String blockName = TestDataUtils.getOptional(data, "В блоке");
            String parameterName = TestDataUtils.get(data, "Параметр");
            HasText textElement;
            if (blockName != null) {
                IContainer container = getElement(blockName);
                textElement = container.getElementByName(elementName).castTo(HasText.class);
            } else {
                textElement = getElement(elementName);
            }
            scenarioData.put(parameterName, textElement.getText());
        }), "Не удалось сохранить значения элементов в параметры");
        List<List<String>> resultList = dataTable.asLists();
        resultList.getFirst().add("Значение");
        for (int i = 1; i < resultList.size(); i++) {
            List<String> row = resultList.get(i);
            row.add(scenarioData.get(row.getLast()));
        }
        DataTable resultTable = DataTable.create(resultList, TextUtils.tableConverter);
        TauLogger.console("Сохранены значения элементов");
        System.out.println(resultTable);
        TauLogger.attachDataTable("Значения элементов", resultTable);
    }
}
