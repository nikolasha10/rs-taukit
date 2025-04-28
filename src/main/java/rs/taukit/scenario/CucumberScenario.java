package rs.taukit.scenario;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.records.Mutex;
import rs.taukit.storage.ScenarioStorage;
import rs.taukit.storage.Storage;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.utils.AllureReport;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class CucumberScenario {
    private final FeatureWrapper featureWrapper;
    private final PickleWrapper pickleWrapper;
    private final ScenarioStorage scenarioStorage = new ScenarioStorage();
    private final String parentSuiteName;
    private final String suiteName;
    private final String subSuiteName;
    private final Set<String> mutexes = new HashSet<>();
    private boolean finished = false;
    private boolean started = false;
    private boolean skipped = false;

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
        TauLogger.console("TEST %s: %s %s", skipped ? "SKIPPED" : "STARTED", featureWrapper, getName());
        scenarioStorage.setScenarioData(new ScenarioData(pickleWrapper.getPickle()));
        scenarioStorage.getScenarioData().put("SCENARIO_TEMP_DIR",
                FileUtils.getScenarioTempDir(parentSuiteName, suiteName, subSuiteName, getName()));
        if (!skipped) {
            runner.runScenario(getPickleWrapper().getPickle());
        }
    }

    public void onFinish(int statusCode, long endMillis) {
        finished = true;
        releaseAllMutexes();
        String testStatus = skipped ? "SKIPPED" : statusCode == 1 ? "PASSED" : "FAILED";
        Config.getDependencies().changeStatus(getTagList(), testStatus.toLowerCase());
        scenarioStorage.getDriverManager().close();
        TauLogger.console("TEST %s: %s %s", testStatus, getFunctionality(), getName());
        AllureReport.editReports(this, testStatus, endMillis);
        TauLogger.collectLogs();
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
        Optional.ofNullable(semaphores.get(mutexName)).ifPresent(mutex ->
                TauLogger.console("""
                                Ожидается снятие запрета на одновременное выполнение действий по ключу '%s'.
                                Установлен %s назад сценарием %s.""",
                        mutexName,
                        TimeStringParser.formatInterval(new Date().getTime() - mutex.startFrom()),
                        mutex.scenarioName()));

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
}
