package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.page.PageObject;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;

import java.util.List;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.driver;

public class AllureBasicFormDataStep {
    private final PageObject pageObject;
    private final DataTable dataTable;
    private final List<Map<String, String>> data;
    private final IWebDriver driver;

    public AllureBasicFormDataStep(DataTable dataTable, PageObject pageObject) {
        this.dataTable = dataTable;
        this.data = dataTable.asMaps();
        this.pageObject = pageObject;
        this.driver = driver();
    }

    @Step("заполнить поля формы значениями:")
    public void fillForm() {
        TauLogger.attachDataTable("Значения полей формы", dataTable);
        data.forEach((row) -> {
            String type = TestDataUtils.get(row, "Тип");
            String name = TestDataUtils.get(row, "Название");
            String value = TestDataUtils.get(row, "Значение");
            String blockName = TestDataUtils.getOptional(row, "Блок");
            String control = TestDataUtils.getOptional(row, "Контроль");
            IHasBlockElements block = blockName == null
                    ? null
                    : pageObject.getElement(blockName).castTo(IHasBlockElements.class);
            try {
                IHasEditableValue element = block == null
                        ? pageObject.getElement(name).castTo(IHasEditableValue.class)
                        : block.getElementByName(name).castTo(IHasEditableValue.class);
                if (control == null) {
                    element.setValue(value);
                } else {
                    element.setValue(value, control);
                }
            } catch (Exception e) {
                TauLogger.fail(e, "Не удалось заполнить %s '%s' в блоке '%s' значением '%s'",
                        type, name, pageObject.getCurrentContext().getElementName(), value);
            }
        });
        TauLogger.report("Поля формы заполнены значениями из таблицы", dataTable);
    }

    private void fillFormBlockAction(String blockName, int blockNumber) {
        TauLogger.attachDataTable("Значения полей блока", dataTable);
        String[] blocks = blockName.split(" -> ");
        IHasBlockElements currentBlock = pageObject.getElement(blocks[0]).castTo(IHasBlockElements.class);
        try {
            driver.waitForCondition(currentBlock::isVisible);
            if (blocks.length > 1) {
                for (int i = 1; i < blocks.length; i++) {
                    currentBlock = currentBlock.getElementByName(blocks[i]).castTo(IHasBlockElements.class);
                    driver.waitForCondition(currentBlock::isVisible);
                }
            }
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться отображения блока '%s'. Метод поиска: %s. Селектор: %s",
                    currentBlock.getElementName(), currentBlock.getModel().getSearchMethod(), currentBlock.getSelector());
        }
        IHasBlockElements block = currentBlock;
        data.forEach((row) -> {
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
                TauLogger.fail(e, "Не удалось заполнить поле \"%s\" в блоке \"%s\" (%d) значением \"%s\"%s",
                        name, blockName, blockNumber, value, controlErrorMessage);
            }
        });
        if (blockNumber == 1) {
            TauLogger.report("Поля блока \"%s\" заполнены значениями из таблицы:", blockName, dataTable);
        } else {
            TauLogger.report("Поля блока \"%s\" (%d) заполнены значениями из таблицы:", blockName, blockNumber, dataTable);
        }
    }

    @Step("заполнить поля формы в блоке '{blockName}' значениями:")
    public void fillFormBlock(String blockName) {
        fillFormBlockAction(blockName, 1);
    }

    @Step("заполнить поля формы в блоке '{blockName}' ({blockNumber}) значениями:")
    public void fillFormBlock(String blockName, int blockNumber) {
        fillFormBlockAction(blockName, blockNumber);
    }
}
