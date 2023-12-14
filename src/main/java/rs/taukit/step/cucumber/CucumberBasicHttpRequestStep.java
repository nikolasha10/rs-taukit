package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicHttpRequestStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicHttpRequestStep {
    private AllureBasicHttpRequestStep httpRequestStep(String name) {
        return new AllureBasicHttpRequestStep(name);
    }

    @И("^отправить POST-запрос \"(.+)\" с параметрами:$")
    public void sendPostRequest(String name, DataTable dataTable) {
        httpRequestStep(name).sendPost(eval(dataTable));
    }
    @И("^отправить GET-запрос \"(.+)\" с параметрами:$")
    public void sendGetRequest(String name, DataTable dataTable) {
        httpRequestStep(name).sendGet(eval(dataTable));
    }

    @И("^разобрать ответ запроса \"(.+)\", сохранив значения в параметрах:$")
    public void parseResponseResult(String name, DataTable dataTable) {
        httpRequestStep(name).parseResponse(eval(dataTable));
    }
}
