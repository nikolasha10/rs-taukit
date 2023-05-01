package step.utils;

import rs.taukit.testdata.TestData;
import rs.taukit.driver.DriverManager;
import rs.taukit.step.BasicStory;
import rs.taukit.utils.TauLogger;

public class Utils extends BasicStory {

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
            openPage("Поиск клиента");
        }
        return checkResult;
    }
}
