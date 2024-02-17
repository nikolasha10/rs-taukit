package rs.taukit.step.allure;

import com.microsoft.playwright.TimeoutError;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.exceptions.TableElementNotFoundException;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class AllureBasicTableStep extends BasicUtils {
    private final String tableName;
    private final IHasTableElements tableElement;

    public AllureBasicTableStep(String tableName) {
        this.tableName = tableName;
        this.tableElement = (IHasTableElements) Optional.ofNullable(page().getElement(tableName))
                .orElseThrow(() -> new TableElementNotFoundException(
                        "Не удалось найти таблицу %s",
                        tableName));
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
        AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
        try {
            driver().waitForCondition(() -> {
                try {
                    int rowIndex = tableElement.findRowByConditions(dataTable);
                    scenarioStorage().saveStepAction(() -> tableElement.findRowByConditions(dataTable));
                    TauLogger.report("Найдена строка %d", rowIndex + 1);
                    return true;
                } catch (Exception e) {
                    exceptionAtomicReference.set(e);
                    return false;
                }
            });
        } catch (Exception e) {
            if (exceptionAtomicReference.get() != null) {
                e.addSuppressed(exceptionAtomicReference.get());
            }
            TauLogger.fail(e, "В таблице \"%s\" не удалось найти строку, соответствующую заданным условиям", tableName);
        }
    }

    @Step("в найденной строке сохранить значения элементов:")
    public void saveTableRowElementValue(DataTable dataTable) {
        DataTable resultTable = tableElement.tableRowSaveElementValues(dataTable);
        TauLogger.attachDataTable("Значения элементов", resultTable);
        TauLogger.console("Сохранены значения элементов", resultTable);
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
        TauLogger.console("В таблице \"%s\" ожидается появление строки, соответствующей условиям:", tableName, dataTable);
        AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
        BooleanSupplier conditions = () -> {
            try {
                tableElement.findRowByConditions(dataTable);
                return true;
            } catch (Exception e) {
                exceptionAtomicReference.set(e);
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
            scenarioStorage().clearStepActions();
            scenarioStorage().stopToSaveActions();
        } catch (Exception e) {
            RuntimeException runtimeException = (RuntimeException) e;
            if (runtimeException instanceof TimeoutError) {
                runtimeException = new RuntimeException();
            }
            if (exceptionAtomicReference.get() != null) {
                runtimeException.addSuppressed(exceptionAtomicReference.get());
            }
            if (runtimeException instanceof AutoTestBrokenException) throw runtimeException;
            TauLogger.fail(runtimeException, "В таблице \"%s\" не удалось дождаться строки, соответствующей заданным условиям",
                    tableName);
        }
    }
}
