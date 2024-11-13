package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.HttpRequestManager;

import static rs.taukit.utils.BasicUtils.eval;

public class HttpRequestStep extends HttpRequestManager {

    @И("^отправить POST-запрос \"(.+)\" с параметрами:$")
    public void sendPostRequest(String name, DataTable dataTable) {
        super.init(name);
        super.sendPost(eval(dataTable));
    }

    @И("^отправить PUT-запрос \"(.+)\" с параметрами:$")
    public void sendPutRequest(String name, DataTable dataTable) {
        super.init(name);
        super.sendPut(eval(dataTable));
    }

    @И("^отправить GET-запрос \"(.+)\" с параметрами:$")
    public void sendGetRequest(String name, DataTable dataTable) {
        super.init(name);
        super.sendGet(eval(dataTable));
    }

    @И("^подготовить значения заголовков запроса \"(.+)\":$")
    public void createRequestHeaders(String name, DataTable dataTable) {
        super.init(name);
        super.createRequestHeaders(eval(dataTable));
    }

    @И("^подготовить значения параметров запроса \"(.+)\":$")
    public void createRequestParams(String name, DataTable dataTable) {
        super.init(name);
        super.createRequestParams(eval(dataTable));
    }

    @И("^разобрать ответ запроса \"(.+)\", сохранив значения в параметрах:$")
    public void parseResponseResult(String name, DataTable dataTable) {
        super.init(name);
        super.parseResponse(eval(dataTable));
    }

    @И("^разобрать json-ответ запроса \"(.+)\", сохранив результат, как \"(.+)\"$")
    public void parseResponseJsonResult(String name, String parameterName) {
        super.init(name);
        super.parseJsonResponse(parameterName);
    }
}
