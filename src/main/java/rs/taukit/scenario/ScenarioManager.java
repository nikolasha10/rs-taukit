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
import rs.taukit.utils.TimeStringParser;
import rs.taukit.utils.YamlParser;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.scenarioData;
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
            String lastStatus,
            String finished,
            String errorMessage,
            String errorCategory,
            String note,
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
        TestResults testResults = new TestResults(lastStatus, status, finished, errorMessage, errorCategory, note, currentAutotestId, tmsLink);
        testSubSuiteResults.testResults().put(scenarioName, testResults);
        testSuiteResults.testSubSuiteResults().put(subSuiteName, testSubSuiteResults);
        testParentSuiteResults.testSuiteResults().put(suiteName, testSuiteResults);
        parentSuiteResults.put(parentSuiteName, testParentSuiteResults);
    }

    public String generateFinalReport(String tagName) {
        List<String> excludedScenarios = findScenariosByTags("@excluded").stream()
                .map(scenario -> String.format("%s %s %s",
                        scenario.getParentSuiteName(), scenario.getSuiteName(), scenario.getName()))
                .toList();
        List<CucumberScenario> allScenarios = tagName.equals("*")
                ? findAllScenariosExceptTag("@excluded")
                : findScenariosByTags(tagName);
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
                null,
                null,
                new ScenarioData(scenario.getPickleWrapper().getPickle()).getOptional("TestITId"),
                null));
        List<File> resultFiles = FileUtils.getDirFileList("target/allure-results").stream()
                .filter(file -> file.getName().endsWith("result.json"))
                .sorted(Comparator.comparingLong(File::lastModified)).toList();
        ObjectMapper mapper = JsonMapper.builder()
                .defaultPropertyInclusion(JsonInclude.Value.construct(
                        JsonInclude.Include.NON_NULL,
                        JsonInclude.Include.NON_NULL
                )).build();
        Map<String, Map<String, ?>> historyMap = new HashMap<>();
        File historyFile = new File("target/allure-results/history/history.json");
        if (historyFile.exists()) {
            try {
                historyMap = YamlParser.convert(mapper.readValue(historyFile, historyMap.getClass()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Map<String, Map<String, ?>> history = historyMap;
        AtomicReference<Double> start = new AtomicReference<>(new Date().getTime() * 1.0);
        AtomicReference<Double> stop = new AtomicReference<>(0.0);
        AtomicReference<Long> previousStart = new AtomicReference<>(new Date().getTime());
        AtomicReference<Long> previousStop = new AtomicReference<>(0L);
        resultFiles.forEach(file -> {
            Map<String, ?> result = new HashMap<>();
            try {
                result = YamlParser.convert(mapper.readValue(file, result.getClass()));
                List<Map<String, String>> labels = YamlParser.convert(result.get("labels"));
                String historyId = YamlParser.convert(result.get("historyId"));
                String parentSuiteName = labels.get(0).get("value");
                String suiteName = labels.size() > 1 ? labels.get(1).get("value") : "-";
                String subSuiteName = labels.size() > 2 ? labels.get(2).get("value") : "-";
                String scenarioName = result.get("name").toString();
                Predicate<CucumberScenario> predicate = scenario ->
                        scenario.getParentSuiteName().equals(parentSuiteName) &&
                                scenario.getSuiteName().equals(suiteName) &&
                                scenario.getSubSuiteName().equals(subSuiteName) &&
                                scenario.getName().equals(scenarioName);
                if (allScenarios.stream().noneMatch(predicate)) return;
                String status = YamlParser.convert(result.get("status"));
                List<Map<String, ?>> lastHistory = history.get(historyId) == null ? null : YamlParser.convert(history.get(historyId).get("items"));
                String lastStatus = lastHistory == null ? null : lastHistory.getFirst().get("status").toString();
                String finished = String.valueOf(file.lastModified());
                Map<String, String> statusDetails = YamlParser.convert(result.get("statusDetails"));
                String errorMessage = statusDetails.getOrDefault("message", "")
                        .replaceAll("\"", "&quot;")
                        .replaceAll("\\{", "&lbrace;")
                        .replaceAll("}", "&rbrace;");
                String trace = statusDetails.getOrDefault("trace", "");
                String category = "";
                String note = statusDetails.getOrDefault("note", "");
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
                            lastStatus,
                            finished,
                            errorMessage.isEmpty() ? null : errorMessage,
                            category.isEmpty() ? null : category,
                            note.isEmpty() ? null : note,
                            null,
                            tmsLink
                    );
                    double scenarioStart = Double.parseDouble(result.get("start").toString());
                    double scenarioStop = Double.parseDouble(result.get("stop").toString());
                    start.set(Math.min(start.get(), scenarioStart));
                    stop.set(Math.max(stop.get(), scenarioStop));
                    if (lastHistory != null && lastHistory.getFirst() != null) {
                        Map<String, ?> time = YamlParser.convert(lastHistory.getFirst().get("time"));
                        long scenarioPreviousStart = YamlParser.convert(time.get("start"));
                        long scenarioPreviousStop = YamlParser.convert(time.get("stop"));
                        previousStart.set(Math.min(previousStart.get(), scenarioPreviousStart));
                        previousStop.set(Math.max(previousStop.get(), scenarioPreviousStop));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        long duration = (long) (stop.get() - start.get());
        long previousDuration = previousStop.get() - previousStart.get();
        String durationString = TimeStringParser.formatInterval((long) (stop.get() - start.get()));
        String durationDeltaString = TimeStringParser.formatInterval(Math.abs(duration - previousDuration));
        scenarioData().put("testRunDuration", durationString);
        scenarioData().put("durationDelta", (duration > previousDuration ? "+ " : "- ") + durationDeltaString);
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
