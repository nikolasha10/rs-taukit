package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.*;
import rs.taukit.exceptions.TableElementNotFoundException;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;
import rs.taukit.utils.TimeStringParser;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

import static rs.taukit.utils.BasicUtils.scenarioData;

public class TableManager extends StepManager {

    private static List<Integer> getValidetedHeadersNumbers(String headersNumbers) {
        String[] chunks = headersNumbers.split(",");
        List<Integer> indexes = new ArrayList<>();
        for (String chunk : chunks) {
            if (chunk.contains("-")) {
                String[] boundaries = chunk.split("-");
                int left = Integer.parseInt(boundaries[0].trim());
                int right = Integer.parseInt(boundaries[1].trim());
                for (int i = left; i <= right; i++) {
                    indexes.add(i);
                }
            } else {
                indexes.add(Integer.parseInt(chunk.trim()) - 1);
            }
        }
        return indexes;
    }

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
        AtomicReference<Integer> rowNumber = new AtomicReference<>();
        Runnable runnable = () -> {
            IHasTableElements table = waitForTable(tableName);
            rowNumber.set(table.findRowByConditions(dataTable) + 1);
        };
        run(runnable, "В таблице '%s' не удалось найти строку, соответствующую заданным условиям", tableName);
        TauLogger.report("Найдена строка %d", rowNumber.get());
        scenarioStorage.clearStepActions();
        scenarioStorage.stopToSaveActions();
    }

    @Step("в таблице '{tableName}' подсчитать количество строк, соответствующих условиям, и сохранить, как '{parameterName}':")
    protected void countRowsByConditions(String tableName, String parameterName, DataTable dataTable) {
        TauLogger.attach("Условия поиска строк", dataTable);
        Runnable runnable = () -> {
            IHasTableElements table = waitForTable(tableName);
            scenarioData.put(parameterName, Long.toString(table.countRowsByConditions(dataTable)));
        };
        run(runnable, "В таблице '%s' не удалось подсчитать количество строк, соответствующих заданным условиям", tableName);
        TauLogger.report("%s = %s", parameterName, scenarioData.get(parameterName));
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
        TauLogger.console("Нажали на элемент '%s' в таблице '%s'", elementName, table.getElementName());
    }

    @Step("в найденной строке установить чекбокс '{checkboxName}'")
    protected void setCheckbox(String checkboxName) {
        IHasTableElements table = scenarioData.get("контекст таблицы");
        run(() -> table.setCheckbox(checkboxName),
                "Не удалось установить чекбокс '%s' в таблице '%s' на странице '%s'",
                checkboxName, table.getElementName(), getPage().getPageName());
        TauLogger.console("Установлен чекбокс '%s' в таблице '%s'", checkboxName, table.getElementName());

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

    private void tableHeadersCheckConditions(String tableName, List<? extends IElement> headers, DataTable dataTable) {
        TauLogger.attachDataTable("Условия проверки заголовков таблицы", dataTable);
        Map<String, DataTable> failedHeaders = new LinkedHashMap<>();
        IContainer original = getContext();
        headers.forEach(item -> {
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
                String headerText = item.castTo(HasText.class).getText();
                String headerName = headerText.isEmpty()
                        ? "№" + (headers.indexOf(item) + 1)
                        : headerText;
                failedHeaders.put(headerName, notMetConditionTable);
            }
        });
        if (!failedHeaders.isEmpty()) {
            Runnable runnable = () -> failedHeaders.forEach(TauLogger::attachDataTable);
            TauLogger.fail(runnable, "Заголовки таблицы '%s' %s не соответствуют требованиям",
                    tableName, failedHeaders.keySet());

        }
        getPage().replaceCurrentContext(original);
        TauLogger.console("Заголовки таблицы '%s' соответствуют условиям проверки", tableName);
    }

    @Step("в таблице '{tableName}' для заголовков столбцов '{headersNumbers}' проверить выполнение условий:")
    protected void tableHeadersCheckConditions(String tableName, String headersNumbers, DataTable dataTable) {
        List<Integer> numbers = getValidetedHeadersNumbers(headersNumbers);
        IHasTableElements table = getElement(tableName);
        List<? extends IElement> headers = table.getHeadersList().getItemList();
        List<? extends IElement> validated = numbers.stream().map(headers::get).toList();
        tableHeadersCheckConditions(tableName, validated, dataTable);
    }

    @Step("в таблице '{tableName}' для каждого заголовка столбца проверить выполнение условий:")
    protected void tableHeadersCheckConditions(String tableName, DataTable dataTable) {
        IHasTableElements table = getElement(tableName);
        tableHeadersCheckConditions(tableName, table.getHeadersList().getItemList(), dataTable);
    }

    @Step("в таблице '{tableName}' для каждой строки проверить выполнение условий:")
    protected void tableAllRowsCheckConditions(String tableName, DataTable dataTable) {
        TauLogger.attachDataTable("Условия проверки столбца таблицы", dataTable);
        IContainer original = getContext();
        IHasTableElements table = getElement(tableName);
        scenarioData().put("контекст таблицы", table);
        List<IContainer> rows = table.getRowList();
        Map<String, DataTable> failedRows = new LinkedHashMap<>();
        IntStream.range(0, rows.size()).boxed().forEach(index -> {
            IContainer activeRow = table.setActiveRow(index);
            if (activeRow == null) {
                return;
            }
            getPage().setCurrentContext(activeRow);
            List<List<String>> notMetConditionList = new ArrayList<>();
            notMetConditionList.add(dataTable.asLists().getFirst());
            notMetConditionList.getFirst().remove("Ожидание");
            notMetConditionList.getFirst().add("Текущее значение");
            dataTable.asMaps().forEach(conditionData -> {
                TestCondition testCondition = new TestCondition(conditionData).waitForResult();
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
                failedRows.put("Строка " + (index + 1), notMetConditionTable);
            }
            getPage().setCurrentContext(original);
        });
        if (!failedRows.isEmpty()) {
            Runnable runnable = () -> failedRows.forEach(TauLogger::attachDataTable);
            List<String> rowNumbers = failedRows.keySet().stream()
                    .map(name -> name.replace("Строка ", "")).toList();
            TauLogger.fail(runnable, "Ячейки таблицы '%s' в строках %s не соответствуют требованиям",
                    tableName, rowNumbers);

        }
        getPage().replaceCurrentContext(original);
        TauLogger.console("Строки таблицы '%s' соответствуют условиям проверки", tableName);
    }

    @Step("в таблице '{tableName}' для каждой ячейки в столбце № {columnNumber} проверить выполнение условий:")
    protected void tableColumnCellsCheckConditions(String tableName, int columnNumber, DataTable dataTable) {
        TauLogger.attachDataTable("Условия проверки столбца таблицы", dataTable);
        IContainer original = getContext();
        IHasTableElements table = getElement(tableName);
        List<IContainer> rows = table.getRowList();
        Map<String, DataTable> failedRows = new LinkedHashMap<>();
        IntStream.range(0, rows.size()).boxed().forEach(index -> {
            IContainer row = rows.get(index);
            HasListItems cells = row.getElementByName("Столбцы").castTo(HasListItems.class);
            IContainer cell = cells.getItemList().get(columnNumber - 1).castTo(IContainer.class);
            getPage().replaceCurrentContext(cell);
            List<List<String>> notMetConditionList = new ArrayList<>();
            notMetConditionList.add(dataTable.asLists().getFirst());
            notMetConditionList.getFirst().add("Текущее значение");
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
                failedRows.put("Строка " + (index + 1), notMetConditionTable);
            }
        });
        if (!failedRows.isEmpty()) {
            Runnable runnable = () -> failedRows.forEach(TauLogger::attachDataTable);
            List<String> rowNumbers = failedRows.keySet().stream()
                    .map(name -> name.replace("Строка ", "")).toList();
            TauLogger.fail(runnable, "Ячейки таблицы '%s' в столбце %d строк %s не соответствуют требованиям",
                    tableName, columnNumber, rowNumbers);

        }
        getPage().replaceCurrentContext(original);
        TauLogger.console("Строки таблицы '%s' в столбце %d соответствуют условиям проверки", tableName, columnNumber);
    }

    @Step("в найденной строке перейти к столбцу '{headerName}'")
    protected void tableRowSetContext(String headerName) {
        IHasTableElements table = scenarioData.get("контекст таблицы");
        run(() -> table.setContext(headerName),
                "Не удалось переключить контекст на столбец '%s' в найденной строке таблицы '%s' на странице '%s'",
                headerName, table.getElementName(), getPage().getPageName());
        TauLogger.report("[примечание] текущий контекст: столбец '%s' в найденной строке таблицы '%s'",
                headerName, table.getElementName());
    }

    @Step("в найденной строке перейти к контейнеру '{containerName}'")
    protected void tableRowSetContainerContext(String containerName) {
        IHasTableElements table = scenarioData.get("контекст таблицы");
        run(() -> table.setContainerContext(containerName),
                "Не удалось переключить контекст на контейнер '%s' в найденной строке таблицы '%s' на странице '%s'",
                containerName, table.getElementName(), getPage().getPageName());
        TauLogger.report("[примечание] текущий контекст: контейнер '%s' в найденной строке таблицы '%s'",
                containerName, table.getElementName());
    }

    @Step("в таблице '{tableName}' перейти к заголовку столбца № {headerNumber}")
    protected void tableHeaderSetContext(String tableName, int headerNumber) {
        IHasTableElements table = getElement(tableName);
        IContainer cell = table.getHeadersList().getItemList().get(headerNumber - 1).castTo(IContainer.class);
        getPage().setCurrentContext(cell);
        TauLogger.report("[примечание] текущий контекст заголовок № %d таблицы '%s'", headerNumber, tableName);
    }

    @Step("в таблице '{tableName}' перейти к заголовку столбца '{headerName}'")
    protected void tableHeaderSetContext(String tableName, String headerName) {
        IHasTableElements table = getElement(tableName);
        IContainer cell = table.getHeaderByName(headerName);
        getPage().setCurrentContext(cell);
        TauLogger.report("[примечание] текущий контекст заголовок '%s' таблицы '%s'", headerName, tableName);
    }
}
