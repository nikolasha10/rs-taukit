package listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.utils.TauLogger;

import static rs.taukit.utils.BasicUtils.storage;

public class SimpleInvokedMethodListener implements IInvokedMethodListener {
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
        if (!method.getTestMethod().getMethodName().equals("afterMethod")) {
            return;
        }
        int startedCount = storage.getScenarioList().stream().filter(CucumberScenario::isStarted).toList().size();
        int maxThreadCount = context.getSuite().getXmlSuite().getDataProviderThreadCount();
        TauLogger.console("maxThreadCount: %d, startedCount: %d", maxThreadCount, startedCount);
        if (startedCount < maxThreadCount && !storage.getScenarioList().isEmpty()) {
            context.getSuite().run();
        }
    }
}
