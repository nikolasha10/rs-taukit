package step;

import rs.taukit.config.Employee;
import rs.taukit.drivers.DriverManager;
import rs.taukit.utils.TauLogger;

public class Logic extends rs.taukit.step.Story {

    public static boolean isNobodyLoggedIn() {
        return getCurrentUser() == null;
    }

    public static boolean isUserLoggedIn(Employee user) {
        Employee currentUser = getCurrentUser();
        boolean checkResult = currentUser != null && currentUser.getLogin().equals(user.getLogin());
        if (checkResult) {
            TauLogger.report("Вход в приложение выполнен");
        }
        return checkResult;
    }

    public static boolean needToChangeUser(Employee user) {
        Employee currentUser = getCurrentUser();
        boolean checkResult = currentUser != null && !currentUser.getLogin().equals(user.getLogin());
        if (checkResult) {
            DriverManager.getInstance().getDriver().reopenBrowser();
        } else {
            TauLogger.report("Вход в приложение был выполнен ранее");
            openPage("Поиск клиента");
        }
        return checkResult;
    }
}
