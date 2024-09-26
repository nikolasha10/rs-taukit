package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicHttpRequestStep;

import java.util.Optional;

import static rs.taukit.utils.BasicUtils.eval;
import static rs.taukit.utils.BasicUtils.scenarioData;

public class CucumberBasicHttpRequestStep {
    private AllureBasicHttpRequestStep httpRequestStep(String name) {
        String fieldName = "httpRequestStep_" + name;
        AllureBasicHttpRequestStep optional = scenarioData().getOptional(fieldName);
        AllureBasicHttpRequestStep allureBasicHttpRequestStep = Optional.ofNullable(optional)
                .orElse(new AllureBasicHttpRequestStep(name));
        scenarioData().put(fieldName, allureBasicHttpRequestStep);
        return allureBasicHttpRequestStep;
    }

    @И("^отправить POST-запрос \"(.+)\" с параметрами:$")
    public void sendPostRequest(String name, DataTable dataTable) {
        httpRequestStep(name).sendPost(eval(dataTable));
    }

    @И("^отправить PUT-запрос \"(.+)\" с параметрами:$")
    public void sendPutRequest(String name, DataTable dataTable) {
        httpRequestStep(name).sendPut(eval(dataTable));
    }

    @И("^отправить GET-запрос \"(.+)\" с параметрами:$")
    public void sendGetRequest(String name, DataTable dataTable) {
        httpRequestStep(name).sendGet(eval(dataTable));
    }

    @И("^подготовить значения заголовков запроса \"(.+)\":$")
    public void createRequestHeaders(String name, DataTable dataTable) {
        httpRequestStep(name).createRequestHeaders(eval(dataTable));
    }
    @И("^подготовить значения параметров запроса \"(.+)\":$")
    public void createRequestParams(String name, DataTable dataTable) {
        httpRequestStep(name).createRequestParams(eval(dataTable));
    }

    @И("^разобрать ответ запроса \"(.+)\", сохранив значения в параметрах:$")
    public void parseResponseResult(String name, DataTable dataTable) {
        httpRequestStep(name).parseResponse(eval(dataTable));
    }

    @И("^разобрать json-ответ запроса \"(.+)\", сохранив результат, как \"(.+)\"$")
    public void parseResponseJsonResult(String name, String parameterName) {
        httpRequestStep(name).parseJsonResponse(parameterName);
    }
}
