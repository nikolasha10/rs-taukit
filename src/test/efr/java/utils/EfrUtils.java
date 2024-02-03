package utils;

import rs.taukit.step.allure.AllureBasicStep;
import rs.taukit.testdata.TestData;
import rs.taukit.utils.TauLogger;
import step.allure.SubScenario;

public class EfrUtils extends AllureBasicStep {

    public static SubScenario subScenario = SubScenario.getInstance();

    public static void runScenario(String functionality, String scenarioName) {
        subScenario.runScenario(functionality, scenarioName);
    }

    public static boolean isNobodyLoggedIn() {
        return getCurrentUser() == null;
    }

    public static boolean isUserLoggedIn(TestData user) {
        TestData currentUser = getCurrentUser();
        boolean checkResult = currentUser != null && currentUser.get("логин").equals(user.get("логин"));
        if (checkResult) {
            TauLogger.report("Вход в приложение выполнен");
        }
        return checkResult;
    }
}
