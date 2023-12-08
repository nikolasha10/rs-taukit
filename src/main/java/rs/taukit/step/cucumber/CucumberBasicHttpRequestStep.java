package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicHttpRequestStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicHttpRequestStep {
    private AllureBasicHttpRequestStep httpRequestStep(String name, DataTable dataTable) {
        return new AllureBasicHttpRequestStep(name, eval(dataTable));
    }

    @И("^отправить POST-запрос \"(.+)\" с параметрами:$")
    public void sendPostRequest(String name, DataTable dataTable) {
        httpRequestStep(name, eval(dataTable)).sendPost();
    }
}
