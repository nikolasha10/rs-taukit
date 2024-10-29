package rs.taukit.scenario;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import lombok.Getter;
import lombok.Setter;
import rs.taukit.config.Config;
import rs.taukit.storage.ScenarioStorage;
import rs.taukit.testdata.ScenarioData;
import rs.taukit.utils.AllureReport;
import rs.taukit.utils.TauLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private boolean finished = false;
    private boolean started = false;
    @Setter
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

    public String getFunctionality() {
        String f = featureWrapper.toString();
        return f.substring(1, f.length() - 1);
    }

    public List<String> getTagList(){
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
    public void setStarted(){
        this.started = true;
    }

    public Object[] getWrapper(){
        return Arrays.asList(pickleWrapper, featureWrapper).toArray(Object[]::new);
    }
    public void onStart(TestNGCucumberRunner runner) {
        scenarioStorage.getDriverManager().getDriver(Config.getProperty("web.driver.name"));
        TauLogger.setLogPath(getFunctionality(), getName());
        System.out.println();
        TauLogger.console("TEST %s: %s %s", skipped ? "SKIPPED" : "STARTED", featureWrapper, getName());
        scenarioStorage.setScenarioData(new ScenarioData(pickleWrapper.getPickle()));
        if (!skipped) {
            runner.runScenario(getPickleWrapper().getPickle());
        }
    }

    public void onFinish(int statusCode, long endMillis) {
        finished = true;
        String testStatus = skipped ? "SKIPPED" : statusCode == 1 ? "PASSED" : "FAILED";
        Config.getDependencies().changeStatus(getTagList(), testStatus.toLowerCase());
        scenarioStorage.getDriverManager().close();
        TauLogger.console("TEST %s: %s %s", testStatus, getFunctionality(), getName());
        AllureReport.editReports(getName(), getFunctionality(), testStatus.toLowerCase(), endMillis);
        TauLogger.collectLogs();
    }

    public boolean hasEqualName(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        return getFunctionality().equals(getFunctionality()) && getName().equals(getName());
    }

    @Override
    public String toString() {
        String functionality = getFunctionality();
        Matcher matcher = Pattern.compile("\\[(.+)]\\[(.+)]").matcher(functionality);
        if (matcher.matches()) {
            String testSuiteName = matcher.group(1);
            String testGroupName = matcher.group(2);
            return String.format("{tags: %s, suite: %s, group: %s, name: %s}",
                    String.join(" ", getTagList()), testSuiteName, testGroupName, getName());
        }
        return String.format("{tags: %s, suite: %s, name: %s}", String.join(" "), functionality, getName());
    }

    public boolean equals(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper){
        return this.pickleWrapper.equals(pickleWrapper) && this.featureWrapper.equals(featureWrapper);
    }
}
