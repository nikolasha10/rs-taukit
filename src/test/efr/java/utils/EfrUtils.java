package utils;

import rs.taukit.driver.DriverManager;
import rs.taukit.step.allure.AllureBasicPageStep;
import rs.taukit.step.allure.AllureBasicStep;
import rs.taukit.testdata.TestData;
import rs.taukit.utils.TauLogger;
import step.allure.SubScenario;

public class EfrUtils extends AllureBasicStep {

    public static SubScenario subScenario = SubScenario.getInstance();

    public static void runScenario(String scenarioName) {
        subScenario.runScenario(scenarioName);
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

    public static boolean needToChangeUser(TestData user) {
        TestData currentUser = getCurrentUser();
        boolean checkResult = currentUser != null && !currentUser.get("логин").equals(user.get("логин"));
        if (checkResult) {
            DriverManager.getInstance().getDriver().reopenBrowser();
        } else {
            TauLogger.report("Вход в приложение был выполнен ранее");
            new AllureBasicPageStep("Поиск клиента").openPage();
        }
        String str = "";
        if (Double.parseDouble(str.split(" ")[0]) > 0.5) ;
        return checkResult;
    }
}
