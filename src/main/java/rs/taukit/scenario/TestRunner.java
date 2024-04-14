package rs.taukit.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import rs.taukit.config.Config;
import rs.taukit.records.TestGroupResults;
import rs.taukit.records.TestResults;
import rs.taukit.records.TestSuiteResults;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.storage;

public class TestRunner {
    private final TestNGCucumberRunner runner;

    public TestRunner(TestNGCucumberRunner runner) {
        this.runner = runner;
    }

    public Object[][] getScenarioByTags() {
        List<CucumberScenario> scenarios = storage.getScenarioList();
        if (scenarios.isEmpty()) {
            if (storage.isTestsFinished()) {
                scenarios.addAll(findScenariosByTags("@finalReport"));
                List<String> excludedScenarios = findScenariosByTags("@disabled").stream()
                        .map(scenario -> String.format("%s %s %s",
                                scenario.getSuiteName(), scenario.getGroupName(), scenario.getName()))
                        .toList();
                List<CucumberScenario> allScenarios = findAllScenariosExceptTag("@disabled");
                Map<String, TestSuiteResults> suiteResults = new HashMap<>();
                allScenarios.forEach(scenario -> updateSuiteResults(
                        suiteResults,
                        scenario.getSuiteName(),
                        scenario.getGroupName(),
                        scenario.getName(),
                        null,
                        null,
                        null));
                List<File> resultFiles = FileUtils.getDirFileList("target/allure-results").stream()
                        .filter(file -> file.getName().endsWith("result.json"))
                        .sorted((f1, f2) -> Math.toIntExact((f1.lastModified() - f2.lastModified())))
                        .toList();
                ObjectMapper mapper = new JsonMapper();
                resultFiles.forEach(file -> {
                    Map<String, ?> result = new HashMap<>();
                    try {
                        result = YamlParser.convert(mapper.readValue(file, result.getClass()));
                        List<Map<String, String>> labels = YamlParser.convert(result.get("labels"));
                        String suiteName = labels.get(0).get("value");
                        String groupName = labels.get(1).get("value");
                        String scenarioName = result.get("name").toString();
                        String status = YamlParser.convert(result.get("status"));
                        String finished = String.valueOf(file.lastModified());
                        Map<String, String> statusDetails = YamlParser.convert(result.get("statusDetails"));
                        String errorMessage = statusDetails.getOrDefault("message", "").replaceAll("\"", "&quot;");
                        if (!excludedScenarios.contains(String.format("%s %s %s", suiteName, groupName, scenarioName))) {
                            updateSuiteResults(
                                    suiteResults,
                                    suiteName,
                                    groupName,
                                    scenarioName,
                                    status,
                                    finished,
                                    errorMessage
                            );
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                try {
                    if (!scenarios.isEmpty()) {
                        String json = mapper.writeValueAsString(suiteResults);
                        scenarios.get(0).getScenarioStorage().getScenarioData().put("scenarioData", json);
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String tags = Config.getProperty("scenario.tags").trim();
                scenarios.addAll(findScenariosByTags(tags));
            }
        }
        List<CucumberScenario> readyToRunScenarios = new ArrayList<>(scenarios.stream()
                .filter(CucumberScenario::isReadyForStart).toList());
        List<CucumberScenario> notStarted = scenarios.stream().filter(scenario -> !scenario.isStarted()).toList();
        boolean isRunning = scenarios.stream().anyMatch(CucumberScenario::isStarted);
        if (readyToRunScenarios.isEmpty() && !notStarted.isEmpty() && !isRunning) {
            notStarted.forEach(scenario -> scenario.setSkipped(true));
            readyToRunScenarios.addAll(notStarted);
        }
        Object[][] startList = readyToRunScenarios.stream()
                .map(scenario -> Arrays.asList(scenario.getPickleWrapper(), scenario.getFeatureWrapper())
                        .toArray(Object[]::new))
                .toArray(Object[][]::new);
        readyToRunScenarios.forEach(scenario -> scenario.setStarted(true));
        return startList;
    }

    private void updateSuiteResults(
            Map<String, TestSuiteResults> suiteResults,
            String suiteName,
            String groupName,
            String scenarioName,
            String status,
            String finished,
            String errorMessage){
        suiteName = suiteName.replaceAll("\"", "&quot;");
        groupName = groupName.replaceAll("\"", "&quot;");
        scenarioName = scenarioName.replaceAll("\"", "&quot;");
        if (errorMessage != null){
            errorMessage = errorMessage.replaceAll("\n", "<br>");
        }
        TestResults testResults = new TestResults(status, finished, errorMessage);
        TestSuiteResults testSuiteResults = suiteResults.getOrDefault(suiteName, new TestSuiteResults(new HashMap<>()));
        TestGroupResults testGroupResults = testSuiteResults.testGroupResults().getOrDefault(groupName, new TestGroupResults(new HashMap<>()));
        testGroupResults.testResults().put(scenarioName, testResults);
        testSuiteResults.testGroupResults().put(groupName, testGroupResults);
        suiteResults.put(suiteName, testSuiteResults);
    }

    /**
     * @return список объектов класса CucumberScenario,
     * описывающий сценарии с как минимум одним из тегов, перечисленных в настройке scenario.tags
     */
    private List<CucumberScenario> findScenariosByTags(String tags) {
        String[] tagList = tags.split(",\\s*");
        return Arrays.stream(runner.provideScenarios())
                .filter(scenarioEntity -> Arrays.stream(tagList).anyMatch(tag -> hasTag(scenarioEntity, tag)))
                .map(CucumberScenario::new)
                .collect(Collectors.toList());
    }

    private List<CucumberScenario> findAllScenariosExceptTag(String tags) {
        String[] tagList = tags.split(",\\s*");
        return Arrays.stream(runner.provideScenarios())
                .filter(scenarioEntity -> Arrays.stream(tagList).noneMatch(tag -> hasTag(scenarioEntity, tag)))
                .map(CucumberScenario::new)
                .collect(Collectors.toList());
    }

    private boolean hasTag(Object[] scenarioEntity, String tag) {
        return scenarioEntity.length == 2 && ((PickleWrapper) scenarioEntity[0]).getPickle().getTags().contains(tag);
    }

    public void runByTag(PickleWrapper pickleWrapper, FeatureWrapper cucumberFeature) {
        storage.putScenario(storage.getScenario(pickleWrapper, cucumberFeature)).onStart(runner);
    }

    public void afterMethod(int status, long startMillis) {
        CucumberScenario scenario = storage.getScenario();
        scenario.onFinish(scenario.isSkipped() ? 3 : status, startMillis);
    }
}
