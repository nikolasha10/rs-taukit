package rs.taukit.step.allure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.exceptions.HttpRequestNotSentException;
import rs.taukit.records.ApiResponse;
import rs.taukit.testdata.TestData;
import rs.taukit.utils.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AllureBasicHttpRequestStep extends BasicUtils {
    private final String name;

    public AllureBasicHttpRequestStep(String name) {
        this.name = name;
    }

    private static Runnable getRunnableParseResponse(DataTable dataTable, List<List<String>> parsedValues, String responseParameterName) {
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
                .collect(Collectors.toMap(
                        row -> TestDataUtils.get(row, "Параметр"),
                        row -> TestDataUtils.get(row, "Значение")));
        String requestUrl = requestParametersMap.getOrDefault("url", createPostUrl().toString());
        List<List<String>> parameters = new ArrayList<>(dataTable.asLists());
        String content;
        if (requestParametersMap.containsKey("content") && !requestParametersMap.get("content").equalsIgnoreCase("null")) {
            content = requestParametersMap.get("content");
        } else if (requestParametersMap.containsKey("filePath")) {
            String filePath = requestParametersMap.get("filePath");
            String fullPath = Config.getProperty("baseResourcesDir") + "files/" + filePath;
            parameters.add(1, Arrays.asList("requestURL", requestUrl));
            content = eval(FileUtils.readFile(fullPath));
        } else {
            throw new RuntimeException("Отсутствует один из обязательных параметров шага (content или filePath)");
        }
        DataTable modified = DataTable.create(parameters, TextUtils.tableConverter);
        TauLogger.attachDataTable("Параметры запроса", modified);
        TauLogger.attach("Тело запроса", content);
        scenarioStorage().saveStepAction(getRunnableSendPost(requestParametersMap, requestUrl, content));
    }

    @Step("отправить GET-запрос '{this.name}' с параметрами:")
    public void sendGet(DataTable dataTable) {
        Map<String, String> requestParametersMap = dataTable.asMaps().stream()
                .collect(Collectors.toMap(
                        row -> TestDataUtils.get(row, "Параметр"),
                        row -> TestDataUtils.get(row, "Значение")));
        String urlString = TestDataUtils.get(requestParametersMap, "url");
        requestParametersMap.remove("url");
        String query = requestParametersMap.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("&"));
        String uriString = urlString + "?" + query;
        List<List<String>> replacedParametersList = new ArrayList<>();
        replacedParametersList.add(Arrays.asList("Параметр", "Значение"));
        replacedParametersList.add(Arrays.asList("uri", urlString));
        requestParametersMap.forEach((key, value) -> replacedParametersList.add(Arrays.asList(key, value)));
        DataTable replacedParametersTable = DataTable.create(replacedParametersList, TextUtils.tableConverter);
        TauLogger.attachDataTable("Параметры GET-запроса", replacedParametersTable);
        AtomicReference<ApiResponse> responseAtomicReference = new AtomicReference<>();
        AtomicReference<TestData> testDataAtomicReference = new AtomicReference<>();
        Runnable runnable = getRunnableSendGet(uriString, responseAtomicReference, testDataAtomicReference);
        scenarioStorage().saveStepAction(runnable);
        ApiResponse response = responseAtomicReference.get();
        String statusText = response.statusText().isEmpty() ? "" : String.format("(%s)", response.statusText());
        TauLogger.report("Отправлен GET-запрос \"%s\". Статус: %d %s", name, response.status(), statusText);
        TauLogger.attach("Тело ответа", response.text());
        testDataAtomicReference.get().getAttachments().forEach(TauLogger::attach);
    }

    private Runnable getRunnableSendPost(Map<String, String> requestParametersMap, String requestUrl, String content) {
        Runnable runnable = () -> {
            AtomicInteger status = new AtomicInteger();
            try {
                String contentType = TestDataUtils.getOptional(requestParametersMap, "contentType");
                String accept = TestDataUtils.getOptional(requestParametersMap, "accept");
                ApiResponse response;
                if (contentType != null) {
                    response = browser().sendPost(requestUrl, contentType, accept, content);
                } else {
                    response = browser().sendPostJson(requestUrl, content);
                }
                String responseStatusParameterName = String.format("Статус запроса '%s'", name);
                String parameterName = String.format("Результат запроса '%s'", name);
                scenarioData().put(responseStatusParameterName, String.valueOf(response.status()));
                scenarioData().put(parameterName, response.text());
                status.set(response.status());
                if (response.ok()) {
                    LinkedHashMap<String, ?> responseMap = parseJsonResponseToMap(response.text());
                    TestData testData = new TestData(this.name + "_response", responseMap);
                    responseMap.forEach((key, value) -> scenarioData().put(key, testData.getOptional(key)));
                    String statusText = response.statusText().isEmpty() ? "" : String.format("(%s)", response.statusText());
                    TauLogger.report("Отправлен POST-запрос \"%s\". Статус: %d %s", name, response.status(), statusText);
                    TauLogger.attach("Тело ответа", response.text());
                    testData.getAttachments().forEach(TauLogger::attach);
                } else {
                    throw new RuntimeException(String.format(
                            "Не удалось отправить POST-запрос \"%s\". Сервер вернул статус %d (%s)",
                            name, response.status(), response.statusText()));
                }
            } catch (Exception e) {
                throw new HttpRequestNotSentException(e, name, "POST", status.get());
            }
        };
        runnable.run();
        return runnable;
    }

    private Runnable getRunnableSendGet(String uriString, AtomicReference<ApiResponse> responseAtomicReference, AtomicReference<TestData> testDataAtomicReference) {
        Runnable runnable = () -> {
            int status = 0;
            try {
                ApiResponse response = driver().getCurrentBrowser().sendGet(uriString);
                responseAtomicReference.set(response);
                String responseStatusParameterName = String.format("Статус запроса '%s'", name);
                String responseTextParameterName = String.format("Результат запроса '%s'", name);
                scenarioData().put(responseStatusParameterName, String.valueOf(response.status()));
                scenarioData().put(responseTextParameterName, response.text());
                status = response.status();
                if (response.ok()) {
                    LinkedHashMap<String, ?> responseMap = parseJsonResponseToMap(response.text());
                    TestData testData = new TestData(this.name + "_response", responseMap);
                    testDataAtomicReference.set(testData);
                    responseMap.forEach((key, value) -> scenarioData().put(key, testData.get(key)));
                } else {
                    throw new RuntimeException(String.format("Сервер вернул статус %d (%s)",
                            response.status(), response.statusText()));
                }
            } catch (Exception e) {
                throw new HttpRequestNotSentException(e, name, "GET", status);
            }
        };
        runnable.run();
        return runnable;
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
        Runnable runnable = getRunnableParseResponse(dataTable, parsedValues, responseParameterName);
        scenarioStorage().saveStepAction(runnable);
        DataTable parsedValuesTable = DataTable.create(parsedValues, TextUtils.tableConverter);
        TauLogger.attachDataTable("Полученные значения", parsedValuesTable);
        TauLogger.console("Получены значения:", parsedValuesTable);
    }

    @Step("разобрать json-ответ запроса '{this.name}', сохранив результат, как '{parameterName}'")
    public void parseJsonResponse(String parameterName) {
        String responseParameterName = String.format("Результат запроса '%s'", name);
        String responseBody = scenarioData().get(responseParameterName);
        TestData testData = new TestData(parameterName, parseJsonResponseToMap(responseBody));
        scenarioData().put(parameterName, testData);
        testData.getAttachments().forEach(TauLogger::attach);
    }

    private LinkedHashMap<String, ?> parseJsonResponseToMap(String responseBody) {
        ObjectMapper jsonMapper = new JsonMapper();

        if (responseBody.startsWith("[")) {
            LinkedHashMap<String, ?> resultMap = new LinkedHashMap<>();
            List<?> parsedList = new ArrayList<>();
            try {
                parsedList = YamlParser.convert(jsonMapper.readValue(responseBody, parsedList.getClass()));
                for (int i = 0; i < parsedList.size(); i++) {
                    resultMap.put("Результат_" + i, YamlParser.convert(parsedList.get(i)));
                }
            } catch (JsonProcessingException ignored) {
                // ignored
            }
            return resultMap;
        }
        Map<String, ?> resultMap = new HashMap<>();
        try {
            resultMap = YamlParser.convert(jsonMapper.readValue(responseBody, resultMap.getClass()));
        } catch (JsonProcessingException ignored) {
            // ignored
        }
        return new LinkedHashMap<>(resultMap);
    }
}
