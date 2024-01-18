package rs.taukit.step.allure;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.exceptions.HttpRequestNotSentException;
import rs.taukit.utils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AllureBasicHttpRequestStep extends BasicUtils {
    private final String name;

    public AllureBasicHttpRequestStep(String name) {
        this.name = name;
    }

    @Step("отправить POST-запрос '{this.name}' с параметрами")
    public void sendPost(DataTable dataTable) {
        scenarioData().put(dataTable);
        List<List<String>> parameters = new ArrayList<>(dataTable.asLists());
        parameters.add(1, Arrays.asList("url", createPostUrl().toString()));
        DataTable modified = DataTable.create(parameters, TextUtils.tableConverter);
        TauLogger.attachDataTable("Параметры запроса", modified);
        String fullPath = Config.getProperty("json.files.dir") + name + ".json";
        String content = eval(FileUtils.readFile(fullPath));
        TauLogger.attach("Тело запроса", content);
        try {
            HttpURLConnection connection = (HttpURLConnection) createPostUrl().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            connection.setConnectTimeout(180000);
            connection.setReadTimeout(180000);
            connection.connect();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            writer.write(content);
            writer.flush();
            writer.close();
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == HttpURLConnection.HTTP_OK
                            ? connection.getInputStream()
                            : connection.getErrorStream()));
            String response = br.lines().collect(Collectors.joining());
            TauLogger.report("Отправлен POST-запрос '%s'. Статус: %d (%s).", name, responseCode, responseMessage);
            String parameterName = String.format("Результат запроса '%s'", name);
            scenarioData().put(parameterName, response);
            TauLogger.attach("Тело ответа", response);
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
        AtomicReference<APIResponse> apiResponse = new AtomicReference<>();
        Runnable runnable = () -> {
            APIRequestContext apiRequestContext = driver().getApiRequestContext();
            apiResponse.set(apiRequestContext.get(urlString));
            String parameterName = String.format("Результат запроса '%s'", name);
            scenarioData().put(parameterName, apiResponse.get().text());
        };
        try {
            runnable.run();
            testContext().saveStepAction(runnable);
            if (apiResponse.get().ok()){
                TauLogger.report("Отправлен GET-запрос '%s'. Статус: %d (%s)",
                        name, apiResponse.get().status(), apiResponse.get().statusText());
                TauLogger.attach("Тело ответа", apiResponse.get().text());
            } else {
                throw new RuntimeException(String.format(
                        "Не удалось отправить GET-запрос '%s'. Сервер вернул статус %d (%s)",
                        name, apiResponse.get().status(), apiResponse.get().statusText()));
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

    public URL createURL(String urlString){
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
        testContext().saveStepAction(runnable);
        DataTable parsedValuesTable = DataTable.create(parsedValues, TextUtils.tableConverter);
        TauLogger.attachDataTable("Полученные значения", parsedValuesTable);
        TauLogger.console("Получены значения:");
        System.out.println(parsedValuesTable);
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


}
