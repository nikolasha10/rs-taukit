package step;

import io.cucumber.java.ru.И;

import static rs.taukit.step.utils.BasicUtils.eval;

public class Scenario {

    @И("^войти в \"(ЕФР)\" под учётными данными сотрудника \"(.+)\"$")
    public void loginIntoApplication(String appName, String employeeDesc){
        Story.loginIntoApplication(appName,employeeDesc);
    }
    @И("^найти клиента по серии \"(.+)\" и номеру \"(.+)\" паспорта$")
    public void findClientByPassport(String series, String number){
        Story.findClientByPassport(eval(series), eval(number));
    }
}
