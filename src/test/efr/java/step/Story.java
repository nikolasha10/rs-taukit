package step;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import step.implementer.Application;
import step.implementer.User;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import step.utils.Utils;

import java.util.Optional;

public class Story extends rs.taukit.step.BasicStory {
    @Step("войти в '{appName}' под учётными данными пользователя '{userDescription}'")
    public static void loginIntoApplication(String appName, String userDescription) {
        User user = new User(userDescription);
        Application activeApp = (Application) testContext().getApplication(appName);
        Application app = Optional.ofNullable(activeApp).orElse(new Application(appName));
        if (!app.isUserLoggedIn(user)){
            app.login(user);
            testContext().putApplication(appName, app);
        }
    }

    @Step("{scenarioName}")
    public static void runScenario(String scenarioName) {
        Utils.runScenario(scenarioName);
    }

    @Step("{scenarioName}")
    public static void runScenario(String scenarioName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры сценария", dataTable);
        dataTable.asMaps().forEach(data -> {
            String parameterName = TestDataUtils.get(data,"Параметр");
            String parameterValue = TestDataUtils.get(data, "Значение");
            scenarioData().put(parameterName, parameterValue);
        });
        Utils.runScenario(scenarioName);
    }
}
