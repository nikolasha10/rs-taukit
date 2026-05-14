package rs.taukit.scenario;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.exceptions.ScenarioNotFoundException;
import rs.taukit.records.TestParentSuiteResults;
import rs.taukit.records.TestResults;
import rs.taukit.records.TestSubSuiteResults;
import rs.taukit.records.TestSuiteResults;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.storage;

public class ScenarioManager {
    private static final List<CucumberScenario> scenarioList = new ArrayList<>();
    @Getter
    private static final List<CucumberScenario> requestedScenarioList = new ArrayList<>();
    private TestNGCucumberRunner runner;
    private boolean isStarted = false;
    @Getter
    private boolean finished = false;

    public static ScenarioManager getInstance() {
        return ScenarioManagerHolder.INSTANCE;
    }

    public void init(TestNGCucumberRunner runner) {
        if (this.isStarted) return;
        this.isStarted = true;
        this.runner = runner;
        String requestedTags = Objects.requireNonNull(Config.getProperty("scenario.tags"));
        String testItAction = Optional.ofNullable(Config.getProperty("testIT.autotest.action")).orElse("none");
        String tags = requestedTags;
        switch (testItAction) {
            case "create" -> tags = "@createTestItAutotest";
            case "update" -> tags = "@updateTestItAutotest";
        }
        scenarioList.addAll(findScenariosByTags(tags));
        requestedScenarioList.addAll(findScenariosByTags(requestedTags));
    }

    public synchronized Object[][] getScenariosToStart() {
        List<CucumberScenario> readyToStartList = new ArrayList<>(
                scenarioList.stream().filter(CucumberScenario::isReadyForStart).toList());
        if (readyToStartList.isEmpty() && getRunningCount() == 0) {
            List<CucumberScenario> pendingList = scenarioList.stream().filter(CucumberScenario::isPending).toList();
            if (!pendingList.isEmpty()) {
                pendingList.forEach(CucumberScenario::setSkipped);
                pendingList.forEach(CucumberScenario::setSkipped);
                readyToStartList.addAll(pendingList);
            } else if (Config.getBooleanProperty("generateFinalReport")) {
                List<CucumberScenario> finalReports = findScenariosByTags("@finalReport");
                scenarioList.addAll(finalReports);
                readyToStartList.addAll(finalReports);
                finished = true;
            }
        }
        readyToStartList.forEach(CucumberScenario::setStarted);
        return readyToStartList.stream().map(CucumberScenario::getWrapper).toArray(Object[][]::new);
    }

    /**
     * @return список объектов класса CucumberScenario,
     * описывающий сценарии с как минимум одним из тегов, перечисленных в настройке scenario.tags
     */
    private List<CucumberScenario> findScenariosByTags(String tags) {
        String[] tagList = tags.split(",\\s*");
        return Arrays.stream(runner.provideScenarios())
                .filter(scenarioEntity -> Arrays.stream(tagList).anyMatch(tag -> hasTag(scenarioEntity, tag)))
                .filter(scenarioEntity -> Arrays.stream(tagList).noneMatch(tag -> hasTag(scenarioEntity, "@disabled")))
                .map(CucumberScenario::new)
                .collect(Collectors.toList());
    }

    private boolean hasTag(Object[] scenarioEntity, String tag) {
        return scenarioEntity.length == 2 && ((PickleWrapper) scenarioEntity[0]).getPickle().getTags().contains(tag);
    }

    public void run(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        CucumberScenario cucumberScenario = scenarioList.stream()
                .filter(scenario -> scenario.equals(pickleWrapper, featureWrapper)).findFirst()
                .orElseThrow(() -> new ScenarioNotFoundException(pickleWrapper.getPickle().getName()));
        storage.putScenario(cucumberScenario);
        cucumberScenario.onStart(runner);
    }

    public CucumberScenario getCurrentScenario() {
        return storage.getScenario();
    }

    public long getRunningCount() {
        return scenarioList.stream().filter(scenario -> scenario.isStarted() && !scenario.isFinished()).count();
    }

    public boolean checkDependency(String tag) {
        return scenarioList.stream().anyMatch(scenario -> scenario.getTagList().contains(tag));
    }

    private List<CucumberScenario> findAllScenariosExceptTag(String tags) {
        String[] tagList = tags.split(",\\s*");
        return Arrays.stream(runner.provideScenarios())
                .filter(scenarioEntity -> Arrays.stream(tagList).noneMatch(tag -> hasTag(scenarioEntity, tag)))
                .map(CucumberScenario::new)
                .collect(Collectors.toList());
    }

    private void updateSuiteResults(
            Map<String, TestParentSuiteResults> parentSuiteResults,
            String parentSuiteName,
            String suiteName,
            String subSuiteName,
            String scenarioName,
            String status,
            String finished,
            String errorMessage,
            String errorCategory,
            String autotestId,
            String tmsLink) {
        parentSuiteName = parentSuiteName.replaceAll("\"", "&quot;");
        suiteName = suiteName.replaceAll("\"", "&quot;");
        subSuiteName = subSuiteName.replaceAll("\"", "&quot;");
        scenarioName = scenarioName.replaceAll("\"", "&quot;");
        if (errorMessage != null) {
            errorMessage = errorMessage
                    .replaceAll("\n", "<br>")
                    .replaceAll("\\{", "&lbrace;")
                    .replaceAll("}", "&rbrace;");
        }
        TestParentSuiteResults testParentSuiteResults = parentSuiteResults.getOrDefault(parentSuiteName, new TestParentSuiteResults(new HashMap<>()));
        TestSuiteResults testSuiteResults = testParentSuiteResults.testSuiteResults().getOrDefault(suiteName, new TestSuiteResults(new HashMap<>()));
        TestSubSuiteResults testSubSuiteResults = testSuiteResults.testSubSuiteResults().getOrDefault(subSuiteName, new TestSubSuiteResults(new HashMap<>()));
        TestResults currentResults = testSubSuiteResults.testResults().get(scenarioName);
        String currentAutotestId = Optional.ofNullable(autotestId).orElse(currentResults == null ? null : currentResults.autotestId());
        TestResults testResults = new TestResults(status, finished, errorMessage, errorCategory, currentAutotestId, tmsLink);
        testSubSuiteResults.testResults().put(scenarioName, testResults);
        testSuiteResults.testSubSuiteResults().put(subSuiteName, testSubSuiteResults);
        testParentSuiteResults.testSuiteResults().put(suiteName, testSuiteResults);
        parentSuiteResults.put(parentSuiteName, testParentSuiteResults);
    }

    public String generateFinalReport() {
        List<String> excludedScenarios = findScenariosByTags("@excluded").stream()
                .map(scenario -> String.format("%s %s %s",
                        scenario.getParentSuiteName(), scenario.getSuiteName(), scenario.getName()))
                .toList();
        List<CucumberScenario> allScenarios = findAllScenariosExceptTag("@excluded");
        Map<String, TestParentSuiteResults> parentSuiteResults = new HashMap<>();
        allScenarios.forEach(scenario -> updateSuiteResults(
                parentSuiteResults,
                scenario.getParentSuiteName(),
                scenario.getSuiteName(),
                scenario.getSubSuiteName(),
                scenario.getName(),
                null,
                null,
                null,
                null,
                new ScenarioData(scenario.getPickleWrapper().getPickle()).getOptional("TestITId"),
                null));
        List<File> resultFiles = FileUtils.getDirFileList("target/allure-results").stream()
                .filter(file -> file.getName().endsWith("result.json"))
                .sorted(Comparator.comparingLong(File::lastModified)).toList();
        ObjectMapper mapper = new JsonMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
                String errorMessage = statusDetails.getOrDefault("message", "")
                        .replaceAll("\"", "&quot;")
                        .replaceAll("\\{", "&lbrace;")
                        .replaceAll("}", "&rbrace;");
                String trace = statusDetails.getOrDefault("trace", "");
                String category = "";
                String[] lines = trace.split("Категории: ");
                if (lines.length > 1) {
                    String[] chunks = lines[1].split("\r\n");
                    if (chunks.length == 1) {
                        chunks = lines[1].split("\n");
                    }
                    category = chunks[0].trim();
                }
                List<Map<String, String>> links = YamlParser.convert(result.get("links"));
                String tmsLink = links.stream()
                        .filter(link -> link.containsKey("type"))
                        .filter(link -> link.get("type").equals("tms"))
                        .findFirst().orElse(Map.of("url", "#"))
                        .get("url");
                if (!excludedScenarios.contains(String.format("%s %s %s", parentSuiteName, suiteName, scenarioName))) {
                    updateSuiteResults(
                            parentSuiteResults,
                            parentSuiteName,
                            suiteName,
                            subSuiteName,
                            scenarioName,
                            status,
                            finished,
                            errorMessage.isEmpty() ? null : errorMessage,
                            category.isEmpty() ? null : category,
                            null,
                            tmsLink
                    );
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            return mapper.writeValueAsString(parentSuiteResults);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class ScenarioManagerHolder {
        private static final ScenarioManager INSTANCE = new ScenarioManager();
    }
}
