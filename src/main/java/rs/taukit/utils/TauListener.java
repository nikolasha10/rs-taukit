package rs.taukit.utils;

import rs.taukit.config.Config;
import rs.taukit.scenario.ScenarioManager;

import java.util.Map;
import java.util.function.Consumer;

public class TauListener {
    public static void afterInvocation(String methodName, int maxThreadCount, Runnable suite) {
        if (!methodName.equals("afterMethod")) {
            return;
        }
        ScenarioManager scenarioManager = ScenarioManager.getInstance();
        if (scenarioManager.getRunningCount() < maxThreadCount && !scenarioManager.isFinished()) {
            suite.run();
        }
    }

    public static void onStart(Map<String, String> parameters,
                               Consumer<Integer> setDataProviderThreadCount,
                               Consumer<Map<String, String>> setParameters) {
        if (parameters.isEmpty() || !parameters.containsKey("started")) {
            Config.init("src/test/resources/configs");
            int threadCount = Integer.parseInt(Config.getProperty("threadCount"));
            setDataProviderThreadCount.accept(threadCount);
            parameters.put("started", "true");
            setParameters.accept(parameters);
        }

    }
}
