package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.page.PageObject;
import rs.taukit.utils.TauLogger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AllureBasicFormDataStep {
    private final PageObject pageObject;
    private final DataTable dataTable;
    private final List<Map<String, String>> data;
    private final IWebDriver driver;

    public AllureBasicFormDataStep(DataTable dataTable, PageObject pageObject) {
        this.dataTable = dataTable;
        this.data = dataTable.asMaps();
        this.pageObject = pageObject;
        this.driver = DriverManager.getInstance().getDriver();
    }

    @Step("заполнить поля формы значениями:")
    public void fillForm() {
        TauLogger.attachDataTable("Значения полей формы", dataTable);
        data.forEach((row) -> {
            String type = Optional.ofNullable(row.get("Тип"))
                    .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле 'Тип'"));
            String blockName = row.get("Блок");
            IHasBlockElements block = blockName == null
                    ? null
                    : pageObject.getElement(blockName).castTo(IHasBlockElements.class);
            String name = Optional.ofNullable(row.get("Название"))
                    .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле 'Название'"));
            String value = Optional.ofNullable(row.get("Значение"))
                    .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле 'Значение'"));
            String control = row.getOrDefault("Контроль", "");
            Class<IElement> elementType = ElementType.getType(type).getElementClass();
            IHasEditableValue element = block == null 
                    ? pageObject.replaceElement(name, elementType).castTo(IHasEditableValue.class)
                    : block.getElementByName(name).castTo(IHasEditableValue.class);
            if (control.isEmpty()) {
                element.setValue(value);
            } else {
                element.setValue(value, control);
            }
        });
        TauLogger.report("Поля формы заполнены значениями из таблицы");
        System.out.println(dataTable);
    }

    private void fillFormBlockAction(String blockName, int blockNumber) {
        TauLogger.attachDataTable("Значения полей блока", dataTable);
        String[] blocks = blockName.split(" -> ");
        IHasBlockElements currentBlock = pageObject.getElement(blocks[0]).castTo(IHasBlockElements.class);
        driver.waitForCondition(currentBlock::isVisible);
        if (blocks.length > 1) {
            for (int i = 1; i < blocks.length; i++) {
                currentBlock = currentBlock.getElementByName(1, blocks[i]).castTo(IHasBlockElements.class);
                driver.waitForCondition(currentBlock::isVisible);
            }
        }
        IHasBlockElements block = currentBlock;
        data.forEach((row) -> {
            String name = Optional.ofNullable(row.get("Название"))
                    .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле 'Название'"));
            String value = Optional.ofNullable(row.get("Значение"))
                    .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле 'Значение'"));
            String control = row.get("Контроль");
            IHasEditableValue editableElement = block.getElementByName(blockNumber, name).castTo(IHasEditableValue.class);
            try {
                if (control == null) {
                    editableElement.setValue(value);
                } else {
                    editableElement.setValue(value, control);
                }
            } catch (Exception e) {
                TauLogger.fail(e, "Не удалось заполнить поле \"%s\" в блоке \"%s\" (%d) значением \"%s\"",
                        name, blockName, blockNumber, value);
            }
        });
        if (blockNumber == 1) {
            TauLogger.report("Поля блока \"%s\" заполнены значениями из таблицы:", blockName);
        } else {
            TauLogger.report("Поля блока \"%s\" (%d) заполнены значениями из таблицы:", blockName, blockNumber);
        }
        System.out.println(dataTable);
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
