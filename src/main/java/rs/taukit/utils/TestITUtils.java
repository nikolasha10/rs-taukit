package rs.taukit.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import rs.taukit.config.Config;
import rs.taukit.scenario.CucumberScenario;
import ru.testit.client.api.AttachmentsApi;
import ru.testit.client.api.AutoTestsApi;
import ru.testit.client.invoker.ApiClient;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.invoker.Configuration;
import ru.testit.client.invoker.auth.ApiKeyAuth;
import ru.testit.client.model.*;

import java.io.File;
import java.util.*;

import static rs.taukit.utils.AllureReport.getAllureResultModel;

public class TestITUtils {
    private static CustomAutotestModel getAutotestModel(CustomAutotestModel model, CucumberScenario scenario) {
        model.setExternalId(scenario.getTestId());
        model.setProjectId(UUID.fromString(Objects.requireNonNull(Config.getProperty("test-it.projectId"))));
        String name = scenario.getName();
        String parentSuiteName = scenario.getParentSuiteName();
        String suiteName = scenario.getSuiteName();
        String subSuiteName = scenario.getSubSuiteName();
        if (!subSuiteName.equals("-")) {
            name = String.format("[%s] %s", subSuiteName, name);
        }
        model.setName(name);
        if (!parentSuiteName.equals("-")) {
            model.setNamespace(parentSuiteName);
        }
        if (!suiteName.equals("-")) {
            model.setClassname(scenario.getSuiteName());
        }
        List<File> resultFiles = getFileList(parentSuiteName, suiteName, subSuiteName, name).stream()
                .sorted(Comparator.comparingLong(File::lastModified)).toList();
        File resultFile = resultFiles.getLast();
        ObjectMapper jsonMapper = new JsonMapper();
        Map<String, ?> resultMap = new HashMap<>();
        String resultContent = FileUtils.readFile(resultFile.getAbsolutePath());
        try {
            resultMap = YamlParser.convert(jsonMapper.readValue(resultContent, resultMap.getClass()));
        } catch (JsonProcessingException e) {
            TauLogger.error(e, "Ошибка при чтении файла с отчётом");
        }
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
        try {
            apiInstance.createAutoTest((AutoTestPostModel) autoTestPostModel);
        } catch (ApiException e) {
            TauLogger.error(e, """
                            Exception when calling AutoTestsApi#createAutoTest.
                            Status code: %s
                            Reason: %s
                            Response headers: %s
                    """, e.getCode(), e.getResponseBody(), e.getResponseHeaders());
        }
    }

    public static void updateAutotest(CucumberScenario cucumberScenario) {
        AutoTestsApi apiInstance = getAutoTestsApi();
        CustomAutotestModel autoTestPutModel = getAutotestModel(new CustomAutoTestPutModel(), cucumberScenario);
        try {
            apiInstance.updateAutoTestWithHttpInfo((AutoTestPutModel) autoTestPutModel);
        } catch (ApiException e) {
            TauLogger.error(e, """
                            Exception when calling AutoTestsApi#updateAutotest.
                            Status code: %s
                            Reason: %s
                            Response headers: %s
                    """, e.getCode(), e.getResponseBody(), e.getResponseHeaders());
        }
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

    private static AutoTestModel getAutotestByExternalId(AutoTestPostModel autoTestPostModel) {
        AutoTestsApi apiInstance = getAutoTestsApi();
        UUID projectId = autoTestPostModel.getProjectId();
        String externalId = autoTestPostModel.getExternalId();
        try {
            ApiV2AutoTestsSearchPostRequest autoTestsSearchPostRequest = new ApiV2AutoTestsSearchPostRequest();
            AutotestsSelectModelFilter autotestsSelectModelFilter = new AutotestsSelectModelFilter();
            autotestsSelectModelFilter
                    .addProjectIdsItem(projectId)
                    .addExternalIdsItem(externalId);
            autoTestsSearchPostRequest.setFilter(autotestsSelectModelFilter);
            List<AutoTestModel> result = apiInstance.apiV2AutoTestsSearchPost(
                    null,
                    null,
                    null,
                    null,
                    null,
                    autoTestsSearchPostRequest);
            System.out.println(result);
            return result.getFirst();
        } catch (ApiException e) {
            System.err.println("Exception when calling AutoTestsApi#getAllAutoTests");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            TauLogger.error(e, "Exception when calling AttachmentsApi#addAttachment");
        } catch (IllegalArgumentException iae) {

        }
        return null;
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
