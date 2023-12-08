package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.exceptions.HttpRequestNotSentException;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AllureBasicHttpRequestStep extends BasicUtils {
    private final String name;
    private final DataTable dataTable;

    public AllureBasicHttpRequestStep(String name, DataTable dataTable) {
        this.name = name;
        this.dataTable = dataTable;
        testContext().getScenarioData().put(dataTable);
    }

    @Step("отправить POST-запрос '{this.name}' с параметрами")
    public void sendPost() {
        List<List<String>> parameters = new ArrayList<>(dataTable.asLists());
        parameters.add(1, Arrays.asList("url", createUrl().toString()));
        DataTable modified = DataTable.create(parameters, TextUtils.tableConverter);
        TauLogger.attachDataTable("Параметры запроса", modified);
        String fullPath = Config.getProperty("json.files.dir") + name + ".json";
        String content = eval(FileUtils.readFile(fullPath));
        TauLogger.attach("Тело запроса", content);
        try {
            HttpURLConnection connection = (HttpURLConnection) createUrl().openConnection();
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
            TauLogger.attach("Тело ответа", response);
        } catch (Exception e) {
            throw new HttpRequestNotSentException(e, name, "POST");
        }
    }

    public URL createUrl() {
        String urlString = String.format("%s%s%s",
                Config.getSTAND().getStubUrl(),
                Config.getProperty("stub.api.uri"),
                name);
        try {
            return new URI(urlString).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
