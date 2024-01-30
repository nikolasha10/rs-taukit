package step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.step.allure.AllureBasicStep;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TimeStringParser;
import utils.EfrUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllureEfrStep extends AllureBasicStep {

    @Step("войти в '{appName}' под учётными данными пользователя '{userDescription}'")
    public static void loginIntoApplication(String appName, String userDescription) {
        User user = new User(userDescription);
        Application activeApp = (Application) testContext().getApplication(appName);
        Application app = Optional.ofNullable(activeApp).orElse(new Application(appName));
        if (!app.isUserLoggedIn(user)) {
            app.login(user);
            testContext().putApplication(appName, app);
        } else {
            driver().getCurrentBrowser().navigate(driver().getCurrentBrowser().getPage("Поиск клиента").getPageUrl());
        }
    }

    public static void runScenarioWrap(String scenarioName) {
        String format = "(.+) -> (.+)";
        Matcher matcher = Pattern.compile(format).matcher(scenarioName);
        if (matcher.matches()) {
            runScenario(matcher.group(1), matcher.group(2));
        } else {
            runScenario(scenarioName);
        }
    }

    public static void runScenarioWrap(String scenarioName, DataTable dataTable) {
        String format = "(.+) -> (.+)";
        Matcher matcher = Pattern.compile(format).matcher(scenarioName);
        if (matcher.matches()) {
            runScenario(matcher.group(1), matcher.group(2), dataTable);
        } else {
            runScenario(scenarioName, dataTable);
        }

    }

    public static void runScenarioUntilWrap(String scenarioName, DataTable dataTable) {
        String format = "(.+) -> (.+)";
        Matcher matcher = Pattern.compile(format).matcher(scenarioName);
        if (matcher.matches()) {
            runScenarioUntil(matcher.group(1), matcher.group(2), dataTable);
        } else {
            runScenarioUntil(null, scenarioName, dataTable);
        }

    }

    @Step("выполнить сценарий \"{scenarioName}\"")
    public static void runScenario(String functionality, String scenarioName) {
        EfrUtils.runScenario(functionality, scenarioName);
    }

    @Step("выполнить сценарий \"{scenarioName}\"")
    public static void runScenario(String featureName, String scenarioName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры сценария", dataTable);
        dataTable.asMaps().forEach(data -> {
            String parameterName = TestDataUtils.get(data, "Параметр");
            String parameterValue = TestDataUtils.get(data, "Значение");
            scenarioData().put(parameterName, parameterValue);
        });
        EfrUtils.runScenario(featureName, scenarioName);
    }

    @Step("выполнять сценарий \"{scenarioName}\" до выполнения условий:")
    public static void runScenarioUntil(String featureName, String scenarioName, DataTable dataTable) {
        TauLogger.attachDataTable("Условия успешного выполнения сценария", dataTable);
        List<Map<String, String>> dataMapList = dataTable.asMaps();
        String defaultTimeout = Config.getProperty("searchValueTimeout");
        String timeoutString = dataMapList.stream()
                .filter(row -> row.get("Объект").equalsIgnoreCase("таймаут"))
                .findFirst().orElse(Map.of("Объект", "таймаут", "Ожидание", defaultTimeout))
                .get("Ожидание");
        double timeout = TimeStringParser.getTime(timeoutString);
        BooleanSupplier conditions = () -> dataTable.asMaps().stream()
                .map(data -> new ExpectedResult(data).waitForResult())
                .allMatch(ExpectedResult::isConditionMet);
        BooleanSupplier scenarioPassed = () -> {
            EfrUtils.runScenario(featureName, scenarioName);
            try {
                driver().waitForCondition(conditions, 1);
            } catch (Exception e) {
                if (e instanceof AutoTestBrokenException) throw e;
                ExpectedResult firstUnexpectedResult = dataTable.asMaps().stream()
                        .map(data -> new ExpectedResult(data).waitForResult())
                        .filter(result -> !result.isConditionMet()).findFirst().orElse(null);
                if (firstUnexpectedResult != null) {
                    if (firstUnexpectedResult.getThrownException() != null) {
                        e.addSuppressed(firstUnexpectedResult.getThrownException());
                    }
                    String objectType = firstUnexpectedResult.getObjectType();
                    String objectName = firstUnexpectedResult.getObjectName();
                    String objectState = firstUnexpectedResult.getObjectState();
                    String expectedValue = firstUnexpectedResult.getExpectedValue() == null
                            ? ""
                            : String.format(" '%s'", firstUnexpectedResult.getExpectedValue());
                    TauLogger.report("Не удалось дождаться выполнения условия: %s '%s' %s %s",
                            objectType, objectName, objectState, expectedValue);
                    return false;
                }
            }
            return true;
        };
        driver().waitForCondition(scenarioPassed, timeout);
    }

    @Step("выполнить сценарий \"{scenarioName}\"")
    public static void runScenario(String scenarioName) {
        EfrUtils.runScenario(null, scenarioName);
    }

    @Step("выполнить сценарий \"{scenarioName}\"")
    public static void runScenario(String scenarioName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры сценария", dataTable);
        dataTable.asMaps().forEach(data -> {
            String parameterName = TestDataUtils.get(data, "Параметр");
            String parameterValue = TestDataUtils.get(data, "Значение");
            scenarioData().put(parameterName, parameterValue);
        });
        EfrUtils.runScenario(null, scenarioName);
    }

}
