package rs.taukit.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import rs.taukit.config.Config;
import rs.taukit.records.TestSubSuiteResults;
import rs.taukit.records.TestParentSuiteResults;
import rs.taukit.records.TestResults;
import rs.taukit.records.TestSuiteResults;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
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
        List<CucumberScenario> scenarios = new ArrayList<>(storage.getScenarioList().get().stream().filter(Objects::nonNull).toList());
        if (scenarios.isEmpty()) {
            if (storage.isTestsFinished()) {
                if (Config.getBooleanProperty("generateFinalReport")) {
                    scenarios.addAll(findScenariosByTags("@finalReport"));
                }
                List<String> excludedScenarios = findScenariosByTags("@disabled").stream()
                        .map(scenario -> String.format("%s %s %s",
                                scenario.getParentSuiteName(), scenario.getSuiteName(), scenario.getName()))
                        .toList();
                List<CucumberScenario> allScenarios = findAllScenariosExceptTag("@disabled");
                Map<String, TestParentSuiteResults> parentSuiteResults = new HashMap<>();
                allScenarios.forEach(scenario -> updateSuiteResults(
                        parentSuiteResults,
                        scenario.getParentSuiteName(),
                        scenario.getSuiteName(),
                        scenario.getSubSuiteName(),
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
                        String parentSuiteName = labels.get(0).get("value");
                        String suiteName = labels.size() > 1 ? labels.get(1).get("value") : "-";
                        String subSuiteName = labels.size() > 2 ? labels.get(2).get("value") : "-";
                        String scenarioName = result.get("name").toString();
                        String status = YamlParser.convert(result.get("status"));
                        String finished = String.valueOf(file.lastModified());
                        Map<String, String> statusDetails = YamlParser.convert(result.get("statusDetails"));
                        String errorMessage = statusDetails.getOrDefault("message", "").replaceAll("\"", "&quot;");
                        if (!excludedScenarios.contains(String.format("%s %s %s", parentSuiteName, suiteName, scenarioName))) {
                            updateSuiteResults(
                                    parentSuiteResults,
                                    parentSuiteName,
                                    suiteName,
                                    subSuiteName,
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
                        String json = mapper.writeValueAsString(parentSuiteResults);
                        scenarios.get(0).getScenarioStorage().getScenarioData().put("scenarioData", json);
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String tags = Config.getProperty("scenario.tags").trim();
                scenarios.addAll(findScenariosByTags(tags));
                storage.getScenarioList().set(scenarios);
            }
        }
        List<CucumberScenario> readyToRunScenarios = new ArrayList<>(scenarios.stream()
                .filter(CucumberScenario::isReadyForStart)
                .toList());
        List<CucumberScenario> notStarted = scenarios.stream().filter(scenario -> !scenario.isStarted()).toList();
        boolean isRunning = scenarios.stream().anyMatch(CucumberScenario::isStarted);
        long start = new Date().getTime();
        boolean isReadyToSkipAll = readyToRunScenarios.isEmpty() && !notStarted.isEmpty() && !isRunning;
        while (isReadyToSkipAll && new Date().getTime() - start < 10000){
            scenarios = storage.getScenarioList().get();
            readyToRunScenarios = new ArrayList<>(scenarios.stream()
                    .filter(CucumberScenario::isReadyForStart).toList());
            notStarted = scenarios.stream().filter(scenario -> !scenario.isStarted()).toList();
            isRunning = scenarios.stream().anyMatch(CucumberScenario::isStarted);
            isReadyToSkipAll = readyToRunScenarios.isEmpty() && !notStarted.isEmpty() && !isRunning;
            if (!isReadyToSkipAll){
                TauLogger.console("Костыль сработал");
            }
        }
        if (isReadyToSkipAll) {
            notStarted.forEach(scenario -> scenario.setSkipped(true));
            readyToRunScenarios.addAll(notStarted);
        }
        Object[][] startList = readyToRunScenarios.stream()
                .map(scenario -> Arrays.asList(scenario.getPickleWrapper(), scenario.getFeatureWrapper())
                        .toArray(Object[]::new))
                .toArray(Object[][]::new);
        readyToRunScenarios.forEach(scenario -> scenario.setStarted(true));
        storage.getScenarioList().set(scenarios);
        return startList;
    }

    private void updateSuiteResults(
            Map<String, TestParentSuiteResults> parentSuiteResults,
            String parentSuiteName,
            String suiteName,
            String subSuiteName,
            String scenarioName,
            String status,
            String finished,
            String errorMessage) {
        parentSuiteName = parentSuiteName.replaceAll("\"", "&quot;");
        suiteName = suiteName.replaceAll("\"", "&quot;");
        subSuiteName = subSuiteName.replaceAll("\"", "&quot;");
        scenarioName = scenarioName.replaceAll("\"", "&quot;");
        if (errorMessage != null) {
            errorMessage = errorMessage.replaceAll("\n", "<br>");
        }
        TestResults testResults = new TestResults(status, finished, errorMessage);
        TestParentSuiteResults testParentSuiteResults = parentSuiteResults.getOrDefault(parentSuiteName, new TestParentSuiteResults(new HashMap<>()));
        TestSuiteResults testSuiteResults = testParentSuiteResults.testSuiteResults().getOrDefault(suiteName, new TestSuiteResults(new HashMap<>()));
        TestSubSuiteResults testSubSuiteResults = testSuiteResults.testSubSuiteResults().getOrDefault(subSuiteName, new TestSubSuiteResults(new HashMap<>()));
        testSubSuiteResults.testResults().put(scenarioName, testResults);
        testSuiteResults.testSubSuiteResults().put(subSuiteName, testSubSuiteResults);
        testParentSuiteResults.testSuiteResults().put(suiteName, testSuiteResults);
        parentSuiteResults.put(parentSuiteName, testParentSuiteResults);
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
