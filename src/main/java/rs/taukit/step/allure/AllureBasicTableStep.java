package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.exceptions.TableElementNotFoundException;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;

import java.util.function.BooleanSupplier;

public class AllureBasicTableStep extends BasicUtils {
    private final String tableName;
    private final IHasTableElements tableElement;

    public AllureBasicTableStep(String tableName) {
        this.tableName = tableName;
        Class<IElement> elementType = ElementType.getType("таблица").getElementClass();
        this.tableElement = page().replaceElement(tableName, elementType).castTo(IHasTableElements.class);
        try {
            driver().waitForCondition(tableElement::isVisible);
        } catch (Exception e) {
            throw new TableElementNotFoundException(e, "Не удалось дождаться отображения таблицы \"%s\"", tableName);
        }

    }

    public AllureBasicTableStep(IHasTableElements tableElement) {
        this.tableName = tableElement.getElementName();
        this.tableElement = tableElement;
    }

    @Step("в таблице '{this.tableName}' нажать на '{elementName}' в строке, соответствующей условиям:")
    public void clickOnElementByConditions(String elementName, DataTable conditions) {
        TauLogger.attachDataTable("Условия выбора элемента", conditions);
        try {
            tableElement.clickOnElementByConditions(elementName, conditions);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось нажать на элемент \"%s\" в таблице \"%s\"", elementName, tableName);
        }
    }

    @Step("в таблице '{this.tableName}' найти строку, соответствующую условиям:")
    public void findRowByConditions(DataTable dataTable) {
        TauLogger.attach("Условия поиска строки", dataTable);
        try {
            int rowIndex = tableElement.findRowByConditions(dataTable);
            scenarioStorage().saveStepAction(() -> tableElement.findRowByConditions(dataTable));
            TauLogger.report("Найдена строка %d", rowIndex + 1);
        } catch (Exception e) {
            TauLogger.fail(e, "В таблице \"%s\" не удалось найти строку, соответствующую заданным условиям", tableName);
        }
    }

    @Step("в найденной строке сохранить значения элементов:")
    public void saveTableRowElementValue(DataTable dataTable) {
        DataTable resultTable = tableElement.tableRowSaveElementValues(dataTable);
        TauLogger.attachDataTable("Значения элементов", resultTable);
        System.out.println(resultTable);
    }

    @Step("в найденной строке нажать на '{elementName}'")
    public void clickOnElement(String elementName) {
        Runnable runnable = () -> tableElement.clickOnElement(elementName);
        runnable.run();
        scenarioStorage().saveStepAction(runnable);
        TauLogger.report("Нажали на элемент \"%s\" в таблице \"%s\"", elementName, tableName);
    }

    @Step("в таблице '{this.tableName}' дождаться строки, соответствующей условиям")
    public void waitForTableConditions(DataTable dataTable) {
        TauLogger.attachDataTable("Условия поиска строки", dataTable);
        TauLogger.console("В таблице \"%s\" ожидается появление строки, соответствующей условиям:", tableName);
        System.out.println(dataTable);
        BooleanSupplier conditions = () -> {
            try {
                tableElement.findRowByConditions(dataTable);
                return true;
            } catch (Exception e) {
                return false;
            }
        };
        double elementCheckInterval = Config.getTimeProperty("element.check.interval");
        String timeout = TestDataUtils.getOptional(dataTable.asMaps().get(0), "Ожидание");
        double elementCheckTimeout = timeout == null
                ? Config.getTimeProperty("element.check.timeout")
                : TimeStringParser.getTime(timeout);
        try {
            driver().waitForCondition(conditions, elementCheckInterval, elementCheckTimeout, scenarioStorage().getStepActions());
            TauLogger.report("Условия выполнены");
            scenarioStorage().clearStepActions();
            scenarioStorage().stopToSaveActions();
        } catch (Exception e) {
            if (e instanceof AutoTestBrokenException) throw e;
            TauLogger.fail(e, "В таблице \"%s\" не удалось дождаться строки, соответствующей заданным условиям",
                    tableName);
        }
    }
}
