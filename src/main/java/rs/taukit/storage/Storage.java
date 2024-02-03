package rs.taukit.storage;

import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import lombok.Getter;
import rs.taukit.exceptions.ScenarioNotFoundException;
import rs.taukit.scenario.CucumberScenario;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    private final ThreadLocal<CucumberScenario> scenarioThreadLocal = new ThreadLocal<>();
    @Getter
    private final List<CucumberScenario> scenarioList = new ArrayList<>();

    public static Storage getInstance() {
        return StorageManagerHolder.INSTANCE;
    }

    public CucumberScenario getScenario() {
        return scenarioThreadLocal.get();
    }

    public CucumberScenario getScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        return scenarioList.stream()
                .filter(scenario -> scenario.hasEqualName(pickleWrapper, featureWrapper))
                .findFirst().orElseThrow(() -> new ScenarioNotFoundException(pickleWrapper.getPickle().getName()));
    }

    public void putScenario(CucumberScenario scenario) {
        scenarioThreadLocal.set(scenario);
    }

    public void remove(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        scenarioList.removeIf(scenario -> scenario.hasEqualName(pickleWrapper, featureWrapper));
    }

    private static class StorageManagerHolder {
        public static final Storage INSTANCE = new Storage();
    }
}
