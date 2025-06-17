package rs.taukit.utils;

import rs.taukit.config.Config;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.records.ApiResponse;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.step.manager.HttpRequestManager;
import rs.taukit.testdata.ScenarioData;
import ru.testit.client.api.AttachmentsApi;
import ru.testit.client.api.AutoTestsApi;
import ru.testit.client.invoker.ApiClient;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.invoker.Configuration;
import ru.testit.client.invoker.auth.ApiKeyAuth;
import ru.testit.client.model.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static rs.taukit.utils.AllureReport.getAllureResultModel;

public class TestITUtils {
    private static CustomAutotestModel getAutotestModel(CustomAutotestModel model, CucumberScenario scenario) {
        ScenarioData scenarioData = new ScenarioData(scenario.getPickleWrapper().getPickle());
        scenario.getScenarioStorage().setScenarioData(scenarioData);
        model.setProjectId(UUID.fromString(Objects.requireNonNull(Config.getProperty("test-it.projectId"))));
        String name = scenario.getName();
        String parentSuiteName = scenario.getParentSuiteName();
        String suiteName = scenario.getSuiteName();
        String subSuiteName = scenario.getSubSuiteName();
        if (!subSuiteName.equals("-")) {
            name = String.format("[%s] %s", subSuiteName, name);
        }
        model.setName(name);
        String autotestId = scenarioData.getOptional("TestITId");
        if (autotestId != null) {
            model.setId(UUID.fromString(autotestId));
        }
        model.setExternalId(scenario.getTestId());
        if (!parentSuiteName.equals("-")) {
            model.setNamespace(parentSuiteName);
        }
        if (!suiteName.equals("-")) {
            model.setClassname(scenario.getSuiteName());
        }
        List<File> resultFiles = getFileList(parentSuiteName, suiteName, subSuiteName, name).stream()
                .sorted(Comparator.comparingLong(File::lastModified)).toList();
        if (resultFiles.isEmpty()) {
            throw new AutoTestBrokenException("Не удалось найти результаты выполнения теста %s", scenario.getFullName());
        }
        File resultFile = resultFiles.getLast();
        String resultContent = FileUtils.readFile(resultFile.getAbsolutePath());
        Map<String, ?> resultMap = JsonParser.parse(resultContent);
        List<Map<String, ?>> steps = YamlParser.convert(resultMap.get("steps"));
        model.setSteps(getAutotestStepModel(steps));
        List<LabelPostModel> labelPostModelList = scenario.getTagList().stream().map(tag -> {
            LabelPostModel labelPostModel = new LabelPostModel();
            labelPostModel.setName(tag);
            return labelPostModel;
        }).toList();
        model.setLabels(labelPostModelList);
        return model;
    }

    public static void createAutotest(CucumberScenario cucumberScenario) {
        AutoTestsApi apiInstance = getAutoTestsApi();
        CustomAutotestModel autoTestPostModel = getAutotestModel(new CustomAutoTestPostModel(), cucumberScenario);
        String autotestId = null, externalId = null;

        try {
            AutoTestModel autoTestModel = apiInstance.createAutoTest((AutoTestPostModel) autoTestPostModel);
            autotestId = autoTestModel.getId().toString();
            externalId = autoTestModel.getExternalId();
        } catch (ApiException e) {
            TauLogger.error(e, """
                            Exception when calling AutoTestsApi#createAutoTest.
                            Status code: %s
                            Reason: %s
                            Response headers: %s
                    """, e.getCode(), e.getResponseBody(), e.getResponseHeaders());
        } catch (Exception e){
            if (e.getMessage().contains("The field `lastTestResultStatus` in the JSON string is not defined in the `AutoTestModel` properties")) {
//                TauLogger.console(e.getMessage());
                autotestId = e.getMessage().split("\"id\":\"")[1].split("\"")[0];
                externalId = e.getMessage().split("\"externalId\":\"")[1].split("\"")[0];
//                TauLogger.console("autotestId: %s, externalId: %s", autotestId, externalId);
            } else {
                throw new AutoTestBrokenException("Не удалось создать автотест в системе TestIT", e);
            }
        }
        ScenarioData scenarioData = cucumberScenario.getScenarioStorage().getScenarioData();
        addAutoTestId(externalId, scenarioData);
        linkAutotestToWorkItem(autotestId, cucumberScenario, apiInstance);
    }

    public static void updateAutotest(CucumberScenario cucumberScenario) {
        AutoTestsApi apiInstance = getAutoTestsApi();
        AutoTestPutModel autoTestPutModel = (AutoTestPutModel) getAutotestModel(new CustomAutoTestPutModel(), cucumberScenario);
        try {
            apiInstance.updateAutoTestWithHttpInfo(autoTestPutModel);
        } catch (ApiException e) {
            TauLogger.error(e, """
                            Exception when calling AutoTestsApi#updateAutotest.
                            Status code: %s
                            Reason: %s
                            Response headers: %s
                    """, e.getCode(), e.getResponseBody(), e.getResponseHeaders());
        }
        ScenarioData scenarioData = cucumberScenario.getScenarioStorage().getScenarioData();
        String externalId = autoTestPutModel.getExternalId();
        Optional<UUID> autotestUUID = Optional.ofNullable(autoTestPutModel.getId());
        String autotestId = autotestUUID.isEmpty()
                ? addAutoTestId(externalId, scenarioData)
                : autotestUUID.get().toString();
        linkAutotestToWorkItem(autotestId, cucumberScenario, apiInstance);
    }

    private static String addAutoTestId(String externalId, ScenarioData scenarioData) {
        List<Map<String, ?>> autotestList = getAutotestByExternalId(externalId);
        AtomicReference<String> autotestId = new AtomicReference<>();
        autotestList.stream().filter(autotest -> autotest.get("isDeleted").toString().equals("false")).forEach(autotest -> {
            autotestId.set(autotest.get("id").toString());
            Map<String, String> replacement = Map.of("TestITId", autotestId.get());
            scenarioData.editTestDataFile(replacement);
        });
        return autotestId.get();
    }

    private static void linkAutotestToWorkItem(String autotestId, CucumberScenario cucumberScenario, AutoTestsApi apiInstance) {
        String tmsTag = cucumberScenario.getTagList().stream().filter(tag -> tag.startsWith("@tms("))
                .findFirst().orElse(null);
        if (tmsTag == null) return;
        Map<String, ?> workItem = getWorkItem(tmsTag);
        String workItemId = workItem.get("id").toString();
        List<Map<String, ?>> autotests = YamlParser.convert(workItem.get("autoTests"));
        if (autotests.stream().anyMatch(item -> item.get("id").toString().equals(autotestId))) {
            TauLogger.console("Автотест '%s' уже прикреплён к тест-кейсу '%s'", cucumberScenario.getName(), workItem.get("name").toString());
            return;
        }
        WorkItemIdModel workItemIdModel = new WorkItemIdModel();
        workItemIdModel.setId(workItemId);
        workItemIdModel.id(workItemId);
        try {
            apiInstance.linkAutoTestToWorkItem(autotestId, workItemIdModel);
        } catch (ApiException e) {
            TauLogger.error(e, """
                            Exception when calling AutoTestsApi.linkAutoTestToWorkItem.
                            Status code: %s
                            Reason: %s
                            Response headers: %s
                    """, e.getCode(), e.getResponseBody(), e.getResponseHeaders());
        }
        TauLogger.console("Автотест '%s' успешно привязан к тест-кейсу '%s'", cucumberScenario.getName(), workItem.get("name").toString());
    }

    private static AutoTestsApi getAutoTestsApi() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath(Objects.requireNonNull(Config.getProperty("test-it.basePath")));
        ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("Bearer or PrivateToken");
        token.setApiKey(Objects.requireNonNull(Config.getProperty("test-it.privateToken")));
        token.setApiKeyPrefix("PrivateToken");

        return new AutoTestsApi(defaultClient);
    }

    private static List<File> getFileList(String parentSuiteName, String suiteName, String subSuiteName, String name) {
        File directory = new File(Objects.requireNonNull(Config.getProperty("allure-results.path")));
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(file -> file.getName().endsWith("result.json"))
                .filter(file -> {
                    Map<String, ?> resultModel = getAllureResultModel(file.getAbsolutePath());
                    List<Map<String, String>> labels = YamlParser.convert(resultModel.get("labels"));
                    boolean found = resultModel.get("name").toString().equals(name);
                    if (!parentSuiteName.equals("-") && !labels.isEmpty()) {
                        found &= labels.getFirst().get("value").equals(parentSuiteName);
                        if (!suiteName.equals("-") && labels.size() > 1) {
                            found &= labels.get(1).get("value").equals(suiteName);
                            if (!subSuiteName.equals("-") && labels.size() > 2) {
                                found &= labels.get(2).get("value").equals(subSuiteName);
                            }
                        }
                    }
                    return found;
                }).toList();
    }

    private static List<AutoTestStepModel> getAutotestStepModel(List<Map<String, ?>> steps) {
        List<AutoTestStepModel> atStepModelList = new ArrayList<>();
        steps.stream().filter(step -> !step.get("name").toString().startsWith("[")).forEach(step -> {
            AutoTestStepModel autoTestStepModel = new AutoTestStepModel();
            autoTestStepModel.setTitle(YamlParser.convert(step.get("name")));
            List<Map<String, ?>> subSteps = YamlParser.convert(step.get("steps"));
            if (!subSteps.isEmpty()) {
                autoTestStepModel.setSteps(getAutotestStepModel(subSteps));
            }
            atStepModelList.add(autoTestStepModel);
        });
        return atStepModelList;
    }

    private static List<Map<String, ?>> getAutotestByExternalId(String externalId) {
        String url = String.format("%s/api/v2/autoTests/search", Config.getProperty("test-it.basePath"));
        String requestBody = String.format(
                "{\"filter\":{\"projectIds\":[\"%s\"],\"externalIds\":[\"%s\"]}}",
                Config.getProperty("test-it.projectId"),
                externalId
        );
        HttpRequestManager httpRequestManager = new HttpRequestManager();
        httpRequestManager.init("getAutotestByExternalId");
        httpRequestManager.setHeader("Content-Type", "application/json; charset=utf-8");
        httpRequestManager.setHeader("Accept", "application/json");
        httpRequestManager.setHeader("Authorization", "PrivateToken " + Config.getProperty("test-it.privateToken"));
        ApiResponse apiResponse = httpRequestManager.sendHttpRequest("POST", url, requestBody);
        return JsonParser.parse(apiResponse.text());
    }

    private static Map<String, ?> getWorkItem(String tmsTag) {
        String workItemId = Arrays.stream(tmsTag.split("/")).toList().getLast().replaceAll("\\D", "");
        String url = String.format("%s/api/v2/workItems/%s", Config.getProperty("test-it.basePath"), workItemId);
        HttpRequestManager httpRequestManager = new HttpRequestManager();
        httpRequestManager.init("getAutotestByExternalId");
        httpRequestManager.setHeader("Content-Type", "application/json; charset=utf-8");
        httpRequestManager.setHeader("Accept", "application/json");
        httpRequestManager.setHeader("Authorization", "PrivateToken " + Config.getProperty("test-it.privateToken"));
        ApiResponse apiResponse = httpRequestManager.sendGet(url);
        return JsonParser.parse(apiResponse.text());
    }

    public void getResult() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("Your TMS address");
        defaultClient.setApiKeyPrefix("PrivateToken");
        defaultClient.setApiKey("Your private token");

        AttachmentsApi apiInstance = new AttachmentsApi();
        File file = new File("file_example");
        try {
            AttachmentModel result = apiInstance.apiV2AttachmentsPost(file);
            System.out.println(result);
        } catch (ApiException e) {
            TauLogger.error(e, "Exception when calling AttachmentsApi#addAttachment");
        }
    }
}
