package step;

import io.qameta.allure.Step;
import rs.taukit.testdata.TestData;
import step.utils.Utils;

public class Story extends rs.taukit.step.BasicStory {
    @Step("войти в '{appName}' под учётными данными сотрудника '{TestDataDesc}'")
    public static void loginIntoApplication(String appName, String TestDataDesc) {
        TestData user = testContext().getScenarioData().get("Пользователь");
        if (Utils.isNobodyLoggedIn() || Utils.needToChangeUser(user)) {
            openBrowser();
            openPage("Страница входа в приложение");
            setValue("Имя пользователя", user.get("логин"));
            setValue("Пароль", user.get("пароль"));
            clickElement("Войти");
            waitForPageLoad("Поиск клиента");
            testContext().setCurrentUser(user);
        }
    }

    @Step("найти клиента по серии '{series}' и номеру '{number}' паспорта")
    public static void findClientByPassport(String series, String number) {
        setValue("Серия", series);
        setValue("Номер", number);
        clickElement("Найти");
        waitForPageLoad("Продукты клиента");
    }


}
