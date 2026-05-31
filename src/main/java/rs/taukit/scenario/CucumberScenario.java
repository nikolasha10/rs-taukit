package rs.taukit.scenario;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import lombok.Getter;
import lombok.Setter;
import rs.taukit.config.Config;
import rs.taukit.records.Mutex;
import rs.taukit.storage.ScenarioStorage;
import rs.taukit.storage.Storage;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.utils.AllureReport;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.*;

@Getter
public class CucumberScenario {
    private final FeatureWrapper featureWrapper;
    private final PickleWrapper pickleWrapper;
    @Setter
    private FeatureWrapper currentFeature;
    @Setter
    private PickleWrapper currentPickleWrapper;
    private final ScenarioStorage scenarioStorage = new ScenarioStorage();
    private final String parentSuiteName;
    private final String suiteName;
    private final String subSuiteName;
    private final Set<String> mutexes = new HashSet<>();
    private boolean finished = false;
    private boolean started = false;
    private boolean skipped = false;
    @Setter
    private boolean reportCorrected = false;
    private Runnable correctReportRunner;

    public CucumberScenario(FeatureWrapper featureWrapper, PickleWrapper pickleWrapper) {
        this.featureWrapper = featureWrapper;
        this.pickleWrapper = pickleWrapper;
        String functionality = getFunctionality();
        Matcher matcher = Pattern.compile("(\\[(.+?)])+?").matcher(functionality);
        if (matcher.find()) {
            this.parentSuiteName = matcher.group(2);
            this.suiteName = matcher.find() ? matcher.group(2) : "-";
            this.subSuiteName = matcher.find() ? matcher.group(2) : "-";
        } else {
            this.parentSuiteName = functionality;
            this.suiteName = "-";
            this.subSuiteName = "-";
        }
    }

    public CucumberScenario(Object[] scenarioObject) {
        this((FeatureWrapper) scenarioObject[1], (PickleWrapper) scenarioObject[0]);
    }

    public String getName() {
        return pickleWrapper.getPickle().getName();
    }

    public String getFullName() {
        return String.format("%s %s", getFunctionality(), getName());
    }

    public String getFunctionality() {
        String f = featureWrapper.toString();
        return f.substring(1, f.length() - 1);
    }

    public String getCurrentFunctionality() {
        String f = currentFeature.toString();
        return f.substring(1, f.length() - 1);
    }

    public String getCurrentName() {
        return currentPickleWrapper.getPickle().getName();
    }

    public List<String> getTagList() {
        return pickleWrapper.getPickle().getTags();
    }

    public Map<String, String> getDependencyMap() {
        return Config.getDependencies().getScenarioDependencyMap(getTagList());
    }

    public boolean isWaitingForDependency() {
        Map<String, String> dependencyMap = getDependencyMap();
        Predicate<Map.Entry<String, String>> isNotMet = d -> Config.getDependencies().checkStatus(d.getValue(), d.getKey());
        return !dependencyMap.isEmpty() && dependencyMap.entrySet().stream().anyMatch(isNotMet);
    }

    public boolean isReadyForStart() {
        return !started && !isWaitingForDependency();
    }

    public boolean isPending() {
        return isWaitingForDependency() && !started;
    }

    public void setStarted() {
        this.started = true;
    }

    public void setSkipped() {
        if (!isReadyForStart()) {
            this.skipped = true;
            Config.getDependencies().changeStatus(getTagList(), "skipped");
            this.finished = true;
        } else {
            this.skipped = false;
        }
    }

    public Object[] getWrapper() {
        return Arrays.asList(pickleWrapper, featureWrapper).toArray(Object[]::new);
    }

    public void onStart(TestNGCucumberRunner runner) {
        scenarioStorage.getDriverManager().getDriver(Config.getProperty("web.driver.name"));
        TauLogger.setLogPath(getFunctionality(), getName());
        System.out.println();
        TauLogger.console("TEST %s: %s", skipped ? "SKIPPED" : "STARTED", getFullName());
        scenarioStorage.setScenarioData(new ScenarioData(pickleWrapper.getPickle()));
        scenarioStorage.getScenarioData().put("SCENARIO_TEMP_DIR",
                FileUtils.getScenarioTempDir(parentSuiteName, suiteName, subSuiteName, getName()));
        if (!skipped) {
            runner.runScenario(getPickleWrapper().getPickle());
        }
    }

    public void onFinish(int statusCode, long endMillis) {
        releaseAllMutexes();
        String testStatus = skipped ? "SKIPPED" : statusCode == 1 ? "PASSED" : "FAILED";
        if (scenarioData() != null) {
            Boolean videoSaveTestRun = Config.getBooleanProperty("video.save");
            Boolean videoSaveScenario = scenarioData().getOptional("video.save");
            boolean videoSave = videoSaveTestRun || Optional.ofNullable(videoSaveScenario).orElse(false);
            Path videoPath = videoSave && page() != null ? page().getVideoPath() : null;
            Boolean harFileSave = scenarioData().getOptional("har-file.save");
            if (driver().isBrowserOpen()) {
                try {
                    browser().close();
                } catch (Exception e) {
                    TauLogger.error(e, "Не удалось закрыть браузер");
                }
            }
            if (!driver().isBrowserOpen()) {
                TauLogger.console("Браузер закрыт");
            }
            if ((Config.getBooleanProperty("har-file.save") || Optional.ofNullable(harFileSave).orElse(false))
                    && testStatus.equalsIgnoreCase("failed")) {
                String harPath = driver().getHarFilePath();
                try {
//                Map<String, ?> har = YamlParser.parse(harPath);
//                Map<String, ?> log = YamlParser.convert(har.get("log"));
//                List<Map<String, ?>> entries = YamlParser.convert(log.get("entries"));
//                List<Map<String, ?>> filteredEntries = entries.stream().filter(entry -> {
//                   Map<String, ?> request = YamlParser.convert(entry.get("request"));
//                   return !request.get("url").toString().contains("kaspersky");
//                }).toList();
//                log.put("entries", YamlParser.convert(filteredEntries));
//                har.put("log", YamlParser.convert(log));
//                FileUtils.writeFile(Path.of(harPath), JsonParser.write(har));
                    File harFile = new File(harPath);
                    TauLogger.attachFile(harFile.getParentFile().getName() + ".har", harPath);
                    FileUtils.getDirFileList(harFile.getParentFile().getAbsolutePath())
                            .stream()
                            .filter(file -> file.getName().endsWith(".json") || file.getName().endsWith(".txt"))
                            .forEach(file -> TauLogger.attachJson(file.getName(), FileUtils.readFile(file.getAbsolutePath())));
                } catch (Exception e) {
                    TauLogger.error("Не удалось прикрепить файл HAR", e);
                }
            }
            if (videoPath != null) {
                TauLogger.attachWebmFile(getName() + ".webm", videoPath.toString());
            }
        }
        Config.getDependencies().changeStatus(getTagList(), testStatus.toLowerCase());
        scenarioStorage.getDriverManager().close();
        if (!testStatus.equalsIgnoreCase("skipped")) {
            TauLogger.console("TEST %s: %s", testStatus, getFullName());
        }
        AllureReport.editReports(this, testStatus, endMillis);
        TauLogger.collectLogs();
        finished = true;
    }

    @Override
    public String toString() {
        String functionality = getFunctionality();
        Matcher matcher = Pattern.compile("\\[(.+)]\\[(.+)]").matcher(functionality);
        List<String> tagList = getTagList().stream()
                .filter(tag -> !tag.matches("@(.+)\\(\"(.+)\",\"(.+)\"\\)")).toList();
        if (matcher.matches()) {
            String testSuiteName = matcher.group(1);
            String testGroupName = matcher.group(2);
            return String.format("{tags: %s, suite: %s, group: %s, name: %s}",
                    String.join(" ", tagList), testSuiteName, testGroupName, getName());
        }
        return String.format("{tags: %s, suite: %s, name: %s}", String.join(" ", tagList), functionality, getName());
    }

    public boolean equals(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        return this.pickleWrapper.equals(pickleWrapper) && this.featureWrapper.equals(featureWrapper);
    }

    public String getTestId() {
        return getTagList().stream()
                .map(tag -> tag.substring(1))
                .filter(tag -> tag.matches("[\\d.]+"))
                .toList()
                .getLast();
    }

    public synchronized void setMutex(String mutexName) {
        Map<String, Mutex> semaphores = Storage.getInstance().getSemaphores();
        Optional.ofNullable(semaphores.get(mutexName)).ifPresent(mutex -> {
            if (!getFullName().equals(mutex.scenarioName()) && mutex.semaphore().availablePermits() == 0) {
                TauLogger.console("""
                                Ожидается снятие запрета на одновременное выполнение действий по ключу '%s'.
                                Установлен %s назад сценарием %s.""",
                        mutexName,
                        TimeStringParser.formatInterval(new Date().getTime() - mutex.startFrom()),
                        mutex.scenarioName());
            }
        });
        semaphores.putIfAbsent(mutexName, new Mutex(getFullName(), new Date().getTime(), new Semaphore(1)));
        try {
            mutexes.add(mutexName);
            Semaphore semaphore = semaphores.get(mutexName).semaphore();
            semaphore.acquire();
            TauLogger.console("Установлен запрет на одновременное выполнение действий по ключу '%s'", mutexName);
            semaphores.put(mutexName, new Mutex(getFullName(), new Date().getTime(), semaphore));
        } catch (InterruptedException ie) {
            TauLogger.error("Thread interrupted", ie);
        }
    }

    public synchronized void releaseMutex(String mutexName) {
        Map<String, Mutex> semaphores = Storage.getInstance().getSemaphores();
        Optional.ofNullable(semaphores.get(mutexName)).ifPresent(mutex -> mutex.semaphore().release());
        mutexes.remove(mutexName);
        TauLogger.console("Снят запрет на одновременное выполнение действий по ключу '%s'", mutexName);
    }

    public void releaseAllMutexes() {
        mutexes.forEach(this::releaseMutex);
    }

    public void setCheckpointPassed(String status) {
        Config.getDependencies().setCheckpointPassed(getTagList(), status);
    }

    public String getWeight() {
        List<String> order = Config.getTestOrder();
        String found = order.stream().filter(tag -> getTagList().contains(tag)).findFirst().orElse(null);
        int weight = found == null ? order.size() : order.indexOf(found);
        return String.format("%04d %s", weight, getFullName());
    }

}
