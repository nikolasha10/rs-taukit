package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.exceptions.HttpRequestNotSentException;
import rs.taukit.records.ApiResponse;
import rs.taukit.utils.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AllureBasicHttpRequestStep extends BasicUtils {
    private final String name;

    public AllureBasicHttpRequestStep(String name) {
        this.name = name;
    }

    private static Runnable getRunnable(DataTable dataTable, List<List<String>> parsedValues, String responseParameterName) {
        Runnable runnable = () -> {
            String responseBody = scenarioData().get(responseParameterName);
            parsedValues.clear();
            parsedValues.add(Arrays.asList("Параметр", "Значение"));
            dataTable.asMaps().forEach(data -> {
                String parameterName = TestDataUtils.get(data, "Параметр");
                String regExp = TestDataUtils.get(data, "Регулярное выражение");
                scenarioData().put(parameterName, TextUtils.findByRegExp(responseBody, regExp));
                parsedValues.add(Arrays.asList(parameterName, scenarioData().get(parameterName)));
            });
        };
        runnable.run();
        return runnable;
    }

    @Step("отправить POST-запрос '{this.name}' с параметрами")
    public void sendPost(DataTable dataTable) {
        scenarioData().put(dataTable);
        Map<String, String> requestParametersMap = dataTable.asMaps().stream()
                .collect(Collectors.toMap(row -> row.get("Параметр"), row -> row.get("Значение")));

        String requestUrl = requestParametersMap.getOrDefault("url", createPostUrl().toString());
        String jsonFileName = requestParametersMap.getOrDefault("jsonFileName", name);
        List<List<String>> parameters = new ArrayList<>(dataTable.asLists());
        parameters.add(1, Arrays.asList("requestURL", requestUrl));
        String fullPath = Config.getProperty("json.files.dir") + jsonFileName + ".json";
        parameters.add(2, Arrays.asList("jsonFileName", jsonFileName + ".json"));
        DataTable modified = DataTable.create(parameters, TextUtils.tableConverter);
        TauLogger.attachDataTable("Параметры запроса", modified);
        String content = eval(FileUtils.readFile(fullPath));
        TauLogger.attach("Тело запроса", content);
        try {
            ApiResponse response = driver().getCurrentBrowser().sendPost(requestUrl, content);
            String parameterName = String.format("Результат запроса '%s'", name);
            scenarioData().put(parameterName, response.text());
            if (response.ok()) {
                TauLogger.report("Отправлен POST-запрос \"%s\". Статус: %d (%s)",
                        name, response.status(), response.statusText());
                TauLogger.attach("Тело ответа", response.text());
            } else {
                throw new RuntimeException(String.format(
                        "Не удалось отправить POST-запрос \"%s\". Сервер вернул статус %d (%s)",
                        name, response.status(), response.statusText()));
            }
        } catch (Exception e) {
            throw new HttpRequestNotSentException(e, name, "POST");
        }
    }

    @Step("отправить GET-запрос '{this.name}' с параметрами:")
    public void sendGet(DataTable dataTable) {
        TauLogger.attachDataTable("Параметры GET-запроса", dataTable);
        String urlString = dataTable.asMaps().stream()
                .filter(data -> TestDataUtils.get(data, "Параметр").equalsIgnoreCase("url"))
                .findFirst()
                .orElseThrow(() -> new DataTableFormatMismatchException("Не найден обязательный параметр 'url'"))
                .get("Значение");
        try {
            ApiResponse response = driver().getCurrentBrowser().sendGet(urlString);
            String parameterName = String.format("Результат запроса '%s'", name);
            scenarioData().put(parameterName, response.text());
            if (response.ok()) {
                TauLogger.report("Отправлен GET-запрос \"%s\". Статус: %d (%s)",
                        name, response.status(), response.statusText());
                TauLogger.attach("Тело ответа", response.text());
            } else {
                throw new RuntimeException(String.format(
                        "Не удалось отправить GET-запрос \"%s\". Сервер вернул статус %d (%s)",
                        name, response.status(), response.statusText()));
            }
        } catch (Exception e) {
            throw new HttpRequestNotSentException(e, name, "GET");
        }

    }

    public URL createPostUrl() {
        String urlString = String.format("%s%s%s",
                Config.getSTAND().getStubUrl(),
                Config.getProperty("stub.api.uri"),
                name);
        return createURL(urlString);
    }

    public URL createURL(String urlString) {
        try {
            return new URI(urlString).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Step("разобрать ответ запроса '{this.name}', сохранив значения в параметрах:")
    public void parseResponse(DataTable dataTable) {
        TauLogger.attachDataTable("Параметры поиска результатов", dataTable);
        String responseParameterName = String.format("Результат запроса '%s'", name);
        List<List<String>> parsedValues = new ArrayList<>();
        Runnable runnable = getRunnable(dataTable, parsedValues, responseParameterName);
        scenarioStorage().saveStepAction(runnable);
        DataTable parsedValuesTable = DataTable.create(parsedValues, TextUtils.tableConverter);
        TauLogger.attachDataTable("Полученные значения", parsedValuesTable);
        TauLogger.console("Получены значения:", parsedValuesTable);
    }


}
