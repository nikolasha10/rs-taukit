package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TextUtils;
import rs.taukit.utils.TimeStringParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class ConditionManager extends StepManager {

    @Step("дождаться выполнения условий \"{conditionsName}\":")
    public void waitForConditions(String conditionsName, DataTable dataTable) {
        waitForConditions(dataTable);
    }

    @Step("проверить выполнение условий '{conditionsName}':")
    public void checkConditions(String conditionsName, DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        Map<String, String> expectedHeaders = dataTable.asMaps().getFirst();
        List<List<String>> unexpectedResultList = new ArrayList<>();
        List<String> resultsHeaders = new ArrayList<>(expectedHeaders.keySet().stream().toList());
        if (resultsHeaders.contains("Значение")) {
            resultsHeaders.add("Текущее значение");
        }
        unexpectedResultList.add(resultsHeaders);
        AtomicReference<Exception> exception = new AtomicReference<>();
        dataTable.asMaps().stream()
                .map(data -> new TestCondition(data).checkResult())
                .filter(result -> !result.isConditionMet())
                .forEach(result -> {
                            List<String> unexpected = new ArrayList<>();
                            unexpected.add(result.getObjectType());
                            unexpected.add(result.getObjectName());
                            unexpected.add(result.getObjectState());
                            if (resultsHeaders.contains("Значение")) {
                                unexpected.add(result.getExpectedValue());
                                unexpected.add(result.getCurrentValue());
                            }
                            unexpectedResultList.add(unexpected);
                            if (result.getThrownException() != null) {
                                if (exception.get() == null) {
                                    exception.set(result.getThrownException());
                                } else {
                                    exception.get().addSuppressed(result.getThrownException());
                                }

                            }
                        }
                );
        if (unexpectedResultList.size() > 1) {
            Runnable runnable = () -> {
                DataTable unexpectedResultsTable = DataTable.create(unexpectedResultList, TextUtils.tableConverter);
                TauLogger.console("Результаты неуспешных проверок", unexpectedResultsTable);
                TauLogger.attachDataTable("Результаты неуспешных проверок", unexpectedResultsTable);
            };
            TauLogger.fail(runnable, exception.get(), "Результаты проверки условий '%s' не соответствуют ожидаемым", conditionsName);
        } else {
            TauLogger.report("Условия '%s' выполнены", conditionsName);
        }
        TauLogger.resetNotCritical();
    }

    @Step("дождаться выполнения условий:")
    public void waitForConditionsNoName(DataTable dataTable) {
        waitForConditions(dataTable);
        TauLogger.console("Условия выполнены");
    }

    @Step("дождаться выполнения условий в течение {timeoutString}:")
    public void waitForConditionsWithinTimeIntervalNoName(String timeoutString, DataTable dataTable) {
        waitForConditions(TimeStringParser.getTime(timeoutString), dataTable);
        TauLogger.console("Условия выполнены");
    }

    @Step("дождаться выполнения условий '{conditionsName}' в течение {timeoutString}:")
    public void waitForConditionsWithinTimeIntervalNoName(String conditionsName, String timeoutString, DataTable dataTable) {
        waitForConditions(TimeStringParser.getTime(timeoutString), dataTable);
        TauLogger.console("Условия '%s' выполнены", conditionsName);
    }

    private void waitForConditions(DataTable dataTable) {
        waitForConditions(Config.getTimeProperty("conditions.wait.timeout"), dataTable);
    }

    private void conditionsWait(double conditionsWaitTimeout, DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        TauLogger.console("Ожидается выполнение условий", dataTable);
        BooleanSupplier conditions = () -> dataTable.asMaps().stream()
                .map(data -> new TestCondition(data).waitForResult())
                .allMatch(TestCondition::isConditionMet);
        double conditionWaitTimeout = Config.getTimeProperty("condition.wait.timeout");
        try {
            getDriver().waitForCondition(conditions, conditionWaitTimeout, conditionsWaitTimeout, scenarioStorage.getStepActions());
        } catch (Exception e) {
            System.out.println(dataTable);
            TauLogger.console(e);
            if (e instanceof AutoTestBrokenException) throw e;
            TestCondition firstUnexpectedResult = dataTable.asMaps().stream()
                    .map(data -> new TestCondition(data).waitForResult())
                    .filter(result -> !result.isConditionMet())
                    .findFirst().orElse(null);
            if (firstUnexpectedResult != null) {
                if (firstUnexpectedResult.getThrownException() != null) {
                    e.addSuppressed(firstUnexpectedResult.getThrownException());
                }
                String objectType = firstUnexpectedResult.getObjectType();
                String objectName = firstUnexpectedResult.getObjectName();
                String objectState = firstUnexpectedResult.getObjectState();
                String currentValue = firstUnexpectedResult.getCurrentValue();
                String expectedValue = firstUnexpectedResult.getExpectedValue() == null
                        ? ""
                        : String.format(" '%s'. Текущее значение: %s", firstUnexpectedResult.getExpectedValue(), currentValue);
                TauLogger.fail(e, "Не удалось дождаться выполнения условия: %s '%s' %s %s",
                        objectType, objectName, objectState, expectedValue);
            }
        }
        scenarioStorage.clearStepActions();
        scenarioStorage.stopToSaveActions();
        TauLogger.resetNotCritical();
    }

    private void waitForConditions(double conditionsWaitTimeout, DataTable dataTable) {
        TauLogger.attachDataTable("Ожидаемый результат", dataTable);
        TauLogger.console("Ожидается выполнение условий", dataTable);
        BooleanSupplier conditions = () -> dataTable.asMaps().stream()
                .map(data -> new TestCondition(data).waitForResult())
                .allMatch(TestCondition::isConditionMet);
        try {
            getDriver().waitForCondition(conditions, conditionsWaitTimeout, scenarioStorage.getStepActions());
        } catch (Exception e) {
            TestCondition firstUnexpectedResult = dataTable.asMaps().stream()
                    .map(row -> new TestCondition(row).waitForResult())
                    .filter(result -> !result.isConditionMet())
                    .findFirst().orElse(null);
            if (firstUnexpectedResult != null) {
                if (firstUnexpectedResult.getThrownException() != null) {
                    e.addSuppressed(firstUnexpectedResult.getThrownException());
                }
                String objectType = firstUnexpectedResult.getObjectType();
                String objectName = firstUnexpectedResult.getObjectName();
                String objectState = firstUnexpectedResult.getObjectState();
                String currentValue = firstUnexpectedResult.getCurrentValue();
                String expectedValue = firstUnexpectedResult.getExpectedValue() == null
                        ? ""
                        : String.format(" '%s'. Текущее значение: %s", firstUnexpectedResult.getExpectedValue(), currentValue);
                TauLogger.fail(e, "Не удалось дождаться выполнения условия: %s '%s' %s %s",
                        objectType, objectName, objectState, expectedValue);
            }
        }
        scenarioStorage.clearStepActions();
        scenarioStorage.stopToSaveActions();
        TauLogger.resetNotCritical();
        TauLogger.console("Условия выполнены");
    }
}
