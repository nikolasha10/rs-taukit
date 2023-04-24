package step;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Employee;
import rs.taukit.context.TestContext;
import rs.taukit.utils.TauLogger;

public class Story extends rs.taukit.step.Story {
    @Step("войти в '{appName}' под учётными данными сотрудника '{employeeDesc}'")
    public static void loginIntoApplication(String appName, String employeeDesc) {
        Employee user = new Employee(employeeDesc);
        if (Logic.isNobodyLoggedIn() || Logic.needToChangeUser(user)) {
            openPage("Страница входа в приложение");
            fill("Имя пользователя", user.getLogin());
            fill("Пароль", user.getPassword());
            click("Войти");
            waitForPageLoad("Поиск клиента");
            TestContext.getInstance().setCurrentUser(user);
        }
    }

    @Step("найти клиента по серии '{series}' и номеру '{number}' паспорта")
    public static void findClientByPassport(String series, String number) {
        fill("Серия", series);
        fill("Номер", number);
        click("Найти");
        waitForPageLoad("Продукты клиента");
    }


}
