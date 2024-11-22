package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.HasListItems;
import rs.taukit.element.interfaces.HasText;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.exceptions.TableElementNotFoundException;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;
import rs.taukit.utils.TimeStringParser;

import java.util.*;
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
        run(runnable, "Не удалось нажать на элемент '%s' в таблице '%s'", elementName, tableName);
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

    @Step("в таблице '{tableName}' проверить наличие столбцов:")
    protected void tableHeadersCheck(String tableName, DataTable dataTable) {
        TauLogger.attachDataTable("Список столбцов", dataTable);
        IHasTableElements table = getElement(tableName);
        List<List<String>> notFoundList = new ArrayList<>();
        notFoundList.add(Arrays.asList("Номер", "Название", "Фактический номер"));
        dataTable.asMaps().forEach(row -> {
            String number = TestDataUtils.getOptional(row, "Номер");
            String name = TestDataUtils.get(row, "Название");
            if (!table.checkHeader(name)) {
                notFoundList.add(Arrays.asList(Optional.ofNullable(number).orElse(""), name, ""));
            } else if (number != null && table.getHeaderIndex(name) != Integer.parseInt(number) - 1) {
                notFoundList.add(Arrays.asList(number, name, String.valueOf(table.getHeaderIndex(name) + 1)));
            }
        });
        if (notFoundList.size() > 1) {
            List<String> headersList = table.getHeaderNamesList();
            List<List<String>> headers = new ArrayList<>();
            headers.add(Arrays.asList("Номер", "Название"));
            headersList.forEach(name -> headers.add(Arrays.asList(String.valueOf(headersList.indexOf(name) + 1), name)));
            DataTable headersTable = DataTable.create(headers, TextUtils.tableConverter);
            DataTable result = DataTable.create(notFoundList, TextUtils.tableConverter);
            Runnable runnable = () -> {
                TauLogger.attach("Столбцы, не прошедшие проверку", result);
                TauLogger.attach("Столбцы таблицы", headersTable);
            };
            TauLogger.fail(runnable, "Набор столбцов таблицы '%s' на странице '%s' отличается от ожидаемого",
                    tableName, getPage().getPageName());
        } else {
            TauLogger.report("Столбцы таблицы '%s' соответствуют ожидаемым", tableName);
        }
    }

    @Step("в таблице '{tableName}' для каждого заголовка столбца проверить выполнение условий:")
    protected void tableHeadersCheckConditions(String tableName, DataTable dataTable) {
        TauLogger.attachDataTable("Условия проверки заголовков таблицы", dataTable);
        IHasTableElements table = getElement(tableName);
        HasListItems headers = table.getHeadersList();
        Map<String, DataTable> failedHeaders = new LinkedHashMap<>();
        IContainer original = getContext();
        headers.getItemList().forEach(item -> {
            getPage().replaceCurrentContext(item.castTo(IContainer.class));
            List<List<String>> notMetConditionList = new ArrayList<>();
            notMetConditionList.add(Arrays.asList("Объект", "Название", "Состояние", "Значение", "Текущее значение"));
            dataTable.asMaps().forEach(conditionData -> {
                TestCondition testCondition = new TestCondition(conditionData).checkResult();
                if (!testCondition.isConditionMet()) {
                    notMetConditionList.add(Arrays.asList(
                            testCondition.getObjectType(),
                            testCondition.getObjectName(),
                            testCondition.getObjectState(),
                            testCondition.getExpectedValue(),
                            testCondition.getCurrentValue()));
                }
            });
            if (notMetConditionList.size() > 1) {
                DataTable notMetConditionTable = DataTable.create(notMetConditionList, TextUtils.tableConverter);
                failedHeaders.put(item.castTo(HasText.class).getText(), notMetConditionTable);
            }
        });
        if (!failedHeaders.isEmpty()) {
            Runnable runnable = () -> failedHeaders.forEach(TauLogger::attachDataTable);
            TauLogger.fail(runnable, "Заголовки таблицы '%s' %s не соответствуют требованиям",
                    tableName, failedHeaders.keySet());

        }
        getPage().replaceCurrentContext(original);
    }
}
