package rs.taukit.step.manager;

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.eval;
import static rs.taukit.utils.BasicUtils.scenarioData;

public class HttpRequestManager extends StepManager {
    private String name;
    private String headersParameterName;
    private String queryParameterName;
    private Map<String, String> headers;
    private Map<String, String> queryParams;

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

    public void init(String name) {
        this.name = name;
        this.headersParameterName = "httpRequest_headers_" + name;
        this.queryParameterName = "httpRequest_queryParams_" + name;
        Map<String, String> headers = scenarioData.getOptional(headersParameterName);
        Map<String, String> queryParams = scenarioData.getOptional(queryParameterName);
        this.headers = Optional.ofNullable(headers).orElse(new HashMap<>());
        this.queryParams = Optional.ofNullable(queryParams).orElse(new HashMap<>());
        scenarioData().put(headersParameterName, this.headers);
        scenarioData().put(queryParameterName, this.queryParams);
    }

    @Step("отправить POST-запрос '{this.name}' с параметрами")
    public void sendPost(DataTable dataTable) {
        scenarioData().put(dataTable);
        Map<String, String> requestParametersMap = dataTable.asMaps().stream()
                .collect(Collectors.toMap(
                        row -> TestDataUtils.get(row, "Параметр"),
                        row -> TestDataUtils.get(row, "Значение")));
        String requestUrl = requestParametersMap.get("url");
        List<List<String>> parameters = new ArrayList<>(dataTable.asLists());
        String content;
        if (requestParametersMap.containsKey("content") && !requestParametersMap.get("content").equalsIgnoreCase("null")) {
            content = requestParametersMap.get("content");
        } else if (requestParametersMap.containsKey("filePath")) {
            String filePath = requestParametersMap.get("filePath");
            String fullPath = Config.getProperty("baseResourcesDir") + "files/" + filePath;
            if (!requestParametersMap.containsKey("url")) {
                parameters.add(1, Arrays.asList("url", requestUrl));
            }
            content = eval(FileUtils.readFile(fullPath));
        } else {
            throw new RuntimeException("Отсутствует один из обязательных параметров шага (content или filePath)");
        }
        DataTable modified = DataTable.create(parameters, TextUtils.tableConverter);
        TauLogger.attachDataTable("Параметры шага", modified);
        if (!headers.isEmpty()) {
            TauLogger.attachMap("Заголовки запроса", headers);
        }
        if (!queryParams.isEmpty()) {
            TauLogger.attachMap("Параметры запроса", queryParams);
        }
        TauLogger.attach("Тело запроса", content);
        sendPost(requestParametersMap, requestUrl, content);
        scenarioData().remove(headersParameterName);
        scenarioData().remove(queryParameterName);
    }

    @Step("отправить PUT-запрос '{this.name}' с параметрами")
    public void sendPut(DataTable dataTable) {
        scenarioData().put(dataTable);
        Map<String, String> requestParametersMap = dataTable.asMaps().stream()
                .collect(Collectors.toMap(
                        row -> TestDataUtils.get(row, "Параметр"),
                        row -> TestDataUtils.get(row, "Значение")));
        String requestUrl = requestParametersMap.containsKey("url") ? requestParametersMap.get("url") : createPostUrl().toString();
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
        TauLogger.attachDataTable("Параметры шага", modified);
        if (!headers.isEmpty()) {
            TauLogger.attachMap("Заголовки запроса", headers);
        }
        if (!queryParams.isEmpty()) {
            TauLogger.attachMap("Параметры запроса", queryParams);
        }
        TauLogger.attach("Тело запроса", content);
        sendPut(requestUrl, content);
        scenarioData().remove(headersParameterName);
        scenarioData().remove(queryParameterName);
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
        replacedParametersList.add(Arrays.asList("url", urlString));
        requestParametersMap.forEach((key, value) -> replacedParametersList.add(Arrays.asList(key, value)));
        DataTable replacedParametersTable = DataTable.create(replacedParametersList, TextUtils.tableConverter);
        TauLogger.attachDataTable("Параметры шага", replacedParametersTable);
        if (!headers.isEmpty()) {
            TauLogger.attachMap("Заголовки запроса", headers);
        }
        if (!queryParams.isEmpty()) {
            TauLogger.attachMap("Параметры запроса", queryParams);
        }
        AtomicReference<ApiResponse> responseAtomicReference = new AtomicReference<>();
        AtomicReference<TestData> testDataAtomicReference = new AtomicReference<>();
        sendGet(uriString, responseAtomicReference, testDataAtomicReference);

        ApiResponse response = responseAtomicReference.get();
        String statusText = response.statusText().isEmpty() ? "" : String.format("(%s)", response.statusText());
        TauLogger.report("Отправлен GET-запрос '%s'. Статус: %d %s", name, response.status(), statusText);
        TauLogger.attach("Тело ответа", response.text());
        testDataAtomicReference.get().getAttachments().forEach(TauLogger::attach);
        scenarioData().remove(headersParameterName);
        scenarioData().remove(queryParameterName);
    }

    private void sendPost(Map<String, String> requestParametersMap, String requestUrl, String content) {
        AtomicReference<ApiResponse> response = new AtomicReference<>();
        AtomicReference<TestData> testData = new AtomicReference<>();
        Runnable runnable = () -> {
            AtomicInteger status = new AtomicInteger();
            AtomicReference<HttpRequestNotSentException> finalException = new AtomicReference<>();
            BooleanSupplier condition = () -> {
                try {
                    String contentType = TestDataUtils.getOptional(requestParametersMap, "contentType");
                    String accept = TestDataUtils.getOptional(requestParametersMap, "accept");
                    headers.putIfAbsent("Content-Type", Optional.ofNullable(contentType).orElse("application/json; charset=utf-8"));
                    headers.putIfAbsent("Accept", Optional.ofNullable(accept).orElse("application/json"));
                    response.set(getBrowser().sendPost(requestUrl, headers, queryParams, content));
                    String responseStatusParameterName = String.format("Статус запроса '%s'", name);
                    String parameterName = String.format("Результат запроса '%s'", name);
                    scenarioData().put(responseStatusParameterName, String.valueOf(response.get().status()));
                    scenarioData().put(parameterName, response.get().text());
                    status.set(response.get().status());
                    LinkedHashMap<String, ?> responseMap = parseJsonResponseToMap(response.get().text());
                    testData.set(new TestData(this.name + "_response", responseMap));
                    responseMap.forEach((key, value) -> scenarioData().put(key, testData.get().getOptional(key)));
                } catch (Exception e) {
                    finalException.set(new HttpRequestNotSentException(e, name, "POST", status.get()));
                    return false;
                }
                return true;
            };
            try {
                getDriver().waitForCondition(condition);
            } catch (Exception e) {
                throw finalException.get();
            }
        };
        run(runnable);
        String statusText = response.get().statusText().isEmpty() ? "" : String.format("(%s)", response.get().statusText());
        TauLogger.report("Отправлен POST-запрос '%s'. Статус: %d %s", name, response.get().status(), statusText);
        String accept = headers.get("Accept");
        if (accept != null && accept.equals("application/json")) {
            TauLogger.attachJson("Тело ответа", response.get().text());
        } else {
            TauLogger.attach("Тело ответа", response.get().text());
        }
        testData.get().getAttachments().forEach(TauLogger::attach);
    }

    private void sendPut(String requestUrl, String content) {
        AtomicReference<ApiResponse> response = new AtomicReference<>();
        AtomicReference<TestData> testData = new AtomicReference<>();
        Runnable runnable = () -> {
            AtomicInteger status = new AtomicInteger();
            try {
                response.set(getBrowser().sendPut(requestUrl, headers, queryParams, content));
                String responseStatusParameterName = String.format("Статус запроса '%s'", name);
                String parameterName = String.format("Результат запроса '%s'", name);
                scenarioData().put(responseStatusParameterName, String.valueOf(response.get().status()));
                scenarioData().put(parameterName, response.get().text());
                status.set(response.get().status());
                LinkedHashMap<String, ?> responseMap = parseJsonResponseToMap(response.get().text());
                testData.set(new TestData(this.name + "_response", responseMap));
                responseMap.forEach((key, value) -> scenarioData().put(key, testData.get().getOptional(key)));
            } catch (Exception e) {
                throw new HttpRequestNotSentException(e, name, "PUT", status.get());
            }
        };
        run(runnable);
        String statusText = response.get().statusText().isEmpty() ? "" : String.format("(%s)", response.get().statusText());
        TauLogger.report("Отправлен PUT-запрос '%s'. Статус: %d %s", name, response.get().status(), statusText);
        TauLogger.attach("Тело ответа", response.get().text());
        testData.get().getAttachments().forEach(TauLogger::attach);
    }

    private void sendGet(String uriString, AtomicReference<ApiResponse> responseAtomicReference, AtomicReference<TestData> testDataAtomicReference) {
        Runnable runnable = () -> {
            int status = 0;
            try {
                ApiResponse response = getDriver().getCurrentBrowser().sendGet(uriString, headers, queryParams);
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
                    responseMap.forEach((key, value) -> scenarioData().put(key, testData.getOptional(key)));
                } else {
                    throw new RuntimeException(String.format("Сервер вернул статус %d (%s)",
                            response.status(), response.statusText()));
                }
            } catch (Exception e) {
                throw new HttpRequestNotSentException(e, name, "GET", status);
            }
        };
        run(runnable);
    }

    public URL createPostUrl() {
        String urlString = String.format("%s/%s", Config.getSTAND().getStubUrl(), name);
        TauLogger.console("urlString: %s", urlString);
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
        scenarioStorage.saveStepAction(runnable);
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

    public void createRequestHeaders(DataTable dataTable) {
        dataTable.asMaps().forEach(row -> headers.put(
                TestDataUtils.get(row, "Параметр"), TestDataUtils.get(row, "Значение")));
        scenarioData.put(headersParameterName, headers);
    }

    public void createRequestParams(DataTable dataTable) {
        dataTable.asMaps().forEach(row -> queryParams.put(
                TestDataUtils.get(row, "Параметр"), TestDataUtils.get(row, "Значение")));
        scenarioData.put(queryParameterName, queryParams);
    }

    public URL createUrl(String urlString) {
        try {
            return new URI(urlString).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ApiResponse sendPost(String url, String requestBody) {
        int responseCode = 0;
        try {
            HttpURLConnection connection = (HttpURLConnection) createUrl(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            headers.forEach(connection::setRequestProperty);
            connection.setUseCaches(false);
            connection.setConnectTimeout((int) Config.getTimeProperty("pageLoadingTimeout"));
            connection.setReadTimeout((int) Config.getTimeProperty("pageLoadingTimeout"));
            connection.connect();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write(requestBody);
            writer.flush();
            writer.close();
            responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == HttpURLConnection.HTTP_OK
                            ? connection.getInputStream()
                            : connection.getErrorStream()));
            return new ApiResponse(
                    responseCode == HttpURLConnection.HTTP_OK,
                    responseCode,
                    responseMessage,
                    br.lines().collect(Collectors.joining()));
        } catch (Exception e) {
            throw new HttpRequestNotSentException(e, name, "POST", responseCode);
        }
    }

    public ApiResponse sendGet(String url) {
        int responseCode = 0;
        try {
            HttpURLConnection connection = (HttpURLConnection) createUrl(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            headers.forEach(connection::setRequestProperty);
            connection.setUseCaches(false);
            connection.setConnectTimeout((int) Config.getTimeProperty("pageLoadingTimeout"));
            connection.setReadTimeout((int) Config.getTimeProperty("pageLoadingTimeout"));
            connection.connect();
            responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == HttpURLConnection.HTTP_OK
                            ? connection.getInputStream()
                            : connection.getErrorStream()));
            return new ApiResponse(
                    responseCode == HttpURLConnection.HTTP_OK,
                    responseCode,
                    responseMessage,
                    br.lines().collect(Collectors.joining()));
        } catch (Exception e) {
            throw new HttpRequestNotSentException(e, name, "GET", responseCode);
        }
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }
}
