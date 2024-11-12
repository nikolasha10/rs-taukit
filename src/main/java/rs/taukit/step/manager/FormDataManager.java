package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;

import java.util.Optional;

public class FormDataManager extends StepManager {

    @Step("заполнить поля формы значениями:")
    public void fillForm(DataTable dataTable) {
        TauLogger.attachDataTable("Значения полей формы", dataTable);
        dataTable.asMaps().forEach((row) -> {
            String type = TestDataUtils.get(row, "Тип");
            String name = TestDataUtils.get(row, "Название");
            String value = Optional.ofNullable(TestDataUtils.getOptional(row, "Значение")).orElse("");
            String containerName = TestDataUtils.getOptional(row, "Блок");
            String control = TestDataUtils.getOptional(row, "Контроль");
            IContainer container = containerName == null ? null : getElement(containerName);
            try {
                IHasEditableValue element = container == null
                        ? getElement(name)
                        : container.getElementByName(name).castTo(IHasEditableValue.class);
                if (control == null) {
                    element.setValue(value);
                } else {
                    element.setValue(value, control);
                }
            } catch (Exception e) {
                TauLogger.fail(e, "Не удалось заполнить %s '%s' в контейнере '%s' значением '%s'",
                        type, name, getContext().getElementName(), value);
            }
        });
        TauLogger.report("Поля формы заполнены значениями из таблицы", dataTable);
    }

    private void fillFormBlockAction(String containerName, int containerNumber, DataTable dataTable) {
        TauLogger.attachDataTable("Значения полей блока", dataTable);
        String[] blocks = containerName.split(" -> ");
        IContainer currentBlock = getPage().getElement(blocks[0]).castTo(IContainer.class);
        try {
            getDriver().waitForCondition(currentBlock::isVisible);
            if (blocks.length > 1) {
                for (int i = 1; i < blocks.length; i++) {
                    currentBlock = currentBlock.getElementByName(blocks[i]).castTo(IContainer.class);
                    getDriver().waitForCondition(currentBlock::isVisible);
                }
            }
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться отображения контейнера '%s'. Метод поиска: %s. Селектор: %s",
                    currentBlock.getElementName(), currentBlock.getModel().getSearchMethod(), currentBlock.getSelector());
        }
        IContainer block = currentBlock;
        dataTable.asMaps().forEach((row) -> {
            String name = TestDataUtils.get(row, "Название");
            String value = TestDataUtils.get(row, "Значение");
            String control = TestDataUtils.getOptional(row, "Контроль");
            IHasEditableValue editableElement = block.getElementByName(name).castTo(IHasEditableValue.class);
            try {
                if (control == null) {
                    editableElement.setValue(value);
                } else {
                    editableElement.setValue(value, control);
                }
            } catch (Exception e) {
                String controlErrorMessage = control == null
                        ? ""
                        : String.format(". Текущее значение (%s) не совпадает с контрольным (%s) ",
                        editableElement.getText(), control);
                TauLogger.fail(e, "Не удалось заполнить поле '%s' в контейнере '%s' (%d) значением '%s'%s",
                        name, containerName, containerNumber, value, controlErrorMessage);
            }
        });
        if (containerNumber == 1) {
            TauLogger.report("Поля контейнера '%s' заполнены значениями из таблицы:", containerName, dataTable);
        } else {
            TauLogger.report("Поля контейнера '%s' (%d) заполнены значениями из таблицы:", containerName, containerNumber, dataTable);
        }
    }

    @Step("заполнить поля формы в контейнере '{blockName}' значениями:")
    public void fillFormBlock(String blockName, DataTable dataTable) {
        fillFormBlockAction(blockName, 1, dataTable);
    }

    @Step("заполнить поля формы в контейнере '{blockName}' ({blockNumber}) значениями:")
    public void fillFormBlock(String blockName, int blockNumber, DataTable dataTable) {
        fillFormBlockAction(blockName, blockNumber, dataTable);
    }
}
