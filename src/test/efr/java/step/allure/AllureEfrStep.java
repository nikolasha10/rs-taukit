package step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.step.allure.AllureBasicStep;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import utils.EfrUtils;

import java.util.Optional;
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
            driver().navigate(pageStorage().getPage("Поиск клиента").getPageUrl());
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

    @Step("выполнить сценарий \"{scenarioName}\"")
    public static void runScenario(String scenarioName) {
        EfrUtils.runScenario(scenarioName);
    }

    @Step("выполнить сценарий \"{scenarioName}\"")
    public static void runScenario(String scenarioName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры сценария", dataTable);
        dataTable.asMaps().forEach(data -> {
            String parameterName = TestDataUtils.get(data, "Параметр");
            String parameterValue = TestDataUtils.get(data, "Значение");
            scenarioData().put(parameterName, parameterValue);
        });
        EfrUtils.runScenario(scenarioName);
    }

}
