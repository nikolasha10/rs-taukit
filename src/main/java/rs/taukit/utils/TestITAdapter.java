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

public class TestITAdapter {
    public static void createAutotest(CucumberScenario cucumberScenario) {
        AutoTestsApi apiInstance = getAutoTestsApi();
        AutoTestPostModel autoTestPostModel = new AutoTestPostModel(); // AutoTestPostModel |
        autoTestPostModel.setExternalId(cucumberScenario.getTestId());
        autoTestPostModel.setProjectId(UUID.fromString("86503c57-8b3c-488f-b224-525e740e2713"));
        String name = cucumberScenario.getName();
        String parentSuiteName = cucumberScenario.getParentSuiteName();
        String suiteName = cucumberScenario.getSuiteName();
        String subSuiteName = cucumberScenario.getSubSuiteName();
        autoTestPostModel.setName(name);
        autoTestPostModel.setNamespace(cucumberScenario.getParentSuiteName());
        autoTestPostModel.setClassname(cucumberScenario.getSuiteName());
        List<File> resultFiles = getFileList(parentSuiteName, suiteName, subSuiteName, name).stream()
                .sorted(Comparator.comparingLong(File::lastModified)).toList();
        File resultFile = resultFiles.getFirst();
        ObjectMapper jsonMapper = new JsonMapper();
        Map<String, ?> resultMap = new HashMap<>();
        String resultContent = FileUtils.readFile(resultFile.getAbsolutePath());
        try {
            resultMap = YamlParser.convert(jsonMapper.readValue(resultContent, resultMap.getClass()));
        } catch (JsonProcessingException e) {
            TauLogger.error(e, "Ошибка при чтении файла с отчётом");
        }
        List<Map<String, ?>> steps = YamlParser.convert(resultMap.get("steps"));
        TauLogger.console("steps.size(): %d", steps.size());
        autoTestPostModel.setSteps(getAutotestStepModel(steps));
        List<LabelPostModel> labelPostModelList = cucumberScenario.getTagList().stream().map(tag -> {
            LabelPostModel labelPostModel = new LabelPostModel();
            labelPostModel.setName(tag);
            return labelPostModel;
        }).toList();
        autoTestPostModel.setLabels(labelPostModelList);
        TauLogger.console("autoTestPostModel: %s", autoTestPostModel);
        try {
            AutoTestModel result = apiInstance.createAutoTest(autoTestPostModel);
            TauLogger.console(result.toString());
        } catch (ApiException e) {
            TauLogger.error(e, """
                            Exception when calling AutoTestsApi#createAutoTest.
                            Status code: %s
                            Reason: %s
                            Response headers: %s
                    """, e.getCode(), e.getResponseBody(), e.getResponseHeaders());
        }
    }

    private static AutoTestsApi getAutoTestsApi() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://team-624y.testit.software");

        // Configure API key authorization: Bearer or PrivateToken
        ApiKeyAuth token = (ApiKeyAuth) defaultClient.getAuthentication("Bearer or PrivateToken");
        token.setApiKey("ZWlleWZxQXhGemhROTJDeXdQ");
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
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
        steps.forEach(step -> {
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
