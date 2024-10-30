package rs.taukit.storage;

import rs.taukit.scenario.CucumberScenario;
import rs.taukit.testdata.DataStorage;

import java.util.HashMap;
import java.util.Optional;

import static rs.taukit.utils.BasicUtils.scenarioData;

public class Storage {
    private final ThreadLocal<CucumberScenario> scenarioThreadLocal = new ThreadLocal<>();
    private final DataStorage dataStorage = new DataStorage(new HashMap<>());

    public static Storage getInstance() {
        return StorageManagerHolder.INSTANCE;
    }

    public CucumberScenario getScenario() {
        return scenarioThreadLocal.get();
    }

    public void putScenario(CucumberScenario scenario) {
        scenarioThreadLocal.set(scenario);
    }

    public <T> T get(String key) {
        Optional<T> optional = scenarioData().get(key);
        return optional.orElse(dataStorage.get(key));
    }

    public void put(String key, Object value) {
        dataStorage.put(key, value);
    }

    public String evalObjectField(String template) {
        try {
            return scenarioData().evalObjectField(template);
        } catch (Exception e) {
            try {
                return dataStorage.evalObjectField(template);
            } catch (Exception e2) {
                e2.addSuppressed(e);
                throw e2;
            }
        }
    }

    public <T> T evalObject(String template) {
        try {
            return scenarioData().evalObject(template);
        } catch (Exception ignored) {
            return dataStorage.evalObject(template);
        }
    }

    public String evalString(String template) {
        try {
            return scenarioData().evalString(template);
        } catch (Exception e) {
            try {
                return dataStorage.evalString(template);
            } catch (Exception ignored) {
                throw e;
            }
        }
    }

    private static class StorageManagerHolder {
        public static final Storage INSTANCE = new Storage();
    }

}
