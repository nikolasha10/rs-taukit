package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.exceptions.TableElementNotFoundException;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class TableManager extends StepManager {

    private IHasTableElements waitForTable(String tableName) {
        try {
            getDriver().waitForCondition(() -> getElement(tableName) != null);
            return getElement(tableName);
        } catch (Exception e) {
            throw new TableElementNotFoundException("Не удалось найти таблицу '%s'", tableName);
        }
    }

    @Step("в таблице '{tableName}' нажать на '{elementName}' в строке, соответствующей условиям:")
    public void clickOnElementByConditions(String tableName, String elementName, DataTable conditions) {
        TauLogger.attachDataTable("Условия выбора элемента", conditions);
        Runnable runnable = () -> {
            IHasTableElements table = waitForTable(tableName);
            table.clickOnElementByConditions(elementName, conditions);
        };
        run(runnable,"Не удалось нажать на элемент '%s' в таблице '%s'", elementName, tableName);
    }

    @Step("в таблице '{tableName}' найти строку, соответствующую условиям:")
    public void findRowByConditions(String tableName, DataTable dataTable) {
        TauLogger.attach("Условия поиска строки", dataTable);
        double elementCheckInterval = Config.getTimeProperty("element.check.interval");
        double elementCheckTimeout = Config.getTimeProperty("element.check.timeout");
        AtomicReference<Integer> rowIndex = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();
        Runnable runnable = () -> getDriver().waitForCondition(() -> {
            try {
                IHasTableElements table = waitForTable(tableName);
                rowIndex.set(table.findRowByConditions(dataTable) + 1);
                return true;
            } catch (Exception e) {
                exception.set(e);
                return false;
            }
        }, elementCheckInterval, elementCheckTimeout, scenarioStorage.getStepActions());
        run(runnable, exception.get(),
                "В таблице '%s' не удалось найти строку, соответствующую заданным условиям", tableName);
        TauLogger.report("Найдена строка %d", rowIndex.get());
        scenarioStorage.clearStepActions();
        scenarioStorage.stopToSaveActions();
    }

    @Step("в найденной строке сохранить значения элементов:")
    public void saveTableRowElementValue(DataTable dataTable) {
        IHasTableElements table = scenarioData.get("контекст таблицы");
        DataTable resultTable = table.tableRowSaveElementValues(dataTable);
        TauLogger.attachDataTable("Значения элементов", resultTable);
        TauLogger.console("Сохранены значения элементов", resultTable);
    }

    @Step("в найденной строке нажать на '{elementName}'")
    public void clickOnElement(String elementName) {
        IHasTableElements table = scenarioData.get("контекст таблицы");
        run(() -> table.clickOnElement(elementName),
                "Не удалось нажать на элемент '%s' в таблице '%s' на странице '%s'",
                elementName, table.getElementName(), getPage().getPageName());
        TauLogger.report("Нажали на элемент '%s' в таблице '%s'", elementName, table.getElementName());
    }

    @Step("в таблице '{tableName}' дождаться строки, соответствующей условиям")
    public void waitForTableConditions(String tableName, DataTable dataTable) {
        TauLogger.attachDataTable("Условия поиска строки", dataTable);
        TauLogger.console("В таблице '%s' ожидается появление строки, соответствующей условиям:", tableName, dataTable);
        String timeout = TestDataUtils.getOptional(dataTable.asMaps().getFirst(), "Ожидание");
        double elementCheckInterval = timeout == null
                ? Config.getTimeProperty("element.check.interval")
                : TimeStringParser.getTime(timeout);
        double elementCheckTimeout = Config.getTimeProperty("element.check.timeout");
        AtomicReference<Exception> exception = new AtomicReference<>();
        BooleanSupplier conditions = () -> {
            try {
                IHasTableElements table = getElement(tableName);
                return table.isVisible() && table.findRowByConditions(dataTable) > -1;
            } catch (Exception e) {
                exception.set(e);
                return false;
            }
        };
        Runnable runnable = () -> getDriver().waitForCondition(
                conditions, elementCheckInterval, elementCheckTimeout, scenarioStorage.getStepActions());
        runOnce(runnable, exception.get(),
                "В таблице '%s' не удалось дождаться строки, соответствующей заданным условиям", tableName);
        scenarioStorage.clearStepActions();
        scenarioStorage.stopToSaveActions();
    }
}
