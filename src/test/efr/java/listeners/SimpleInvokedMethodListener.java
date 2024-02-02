package listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import rs.taukit.config.Config;

public class SimpleInvokedMethodListener implements IInvokedMethodListener {
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
        if (method.getTestMethod().getMethodName().equals("afterMethod")) {
            if (Config.getDependencies().isWaiting()) {
                context.getSuite().run();
            }
        }
    }
}
