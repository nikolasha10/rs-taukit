package listeners;

import org.slf4j.MDC;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;
import rs.taukit.config.Config;

import java.util.Map;

public class SuiteListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
        XmlSuite xmlSuite = suite.getXmlSuite();
        Map<String, String> parameters = xmlSuite.getAllParameters();
        if (parameters.isEmpty() || !parameters.containsKey("started")) {
            Config.init("src/test/efr/resources/configs");
            int threadCount = Integer.parseInt(Config.getProperty("threadCount"));
            xmlSuite.setDataProviderThreadCount(threadCount);
            parameters.put("started", "true");
            xmlSuite.setParameters(parameters);
        }
    }

    @Override
    public void onFinish(ISuite suite) {
//        storage.getScenarioList().forEach(scenario -> scenario.getScenarioStorage().getDriverManager().close());
        MDC.remove("logName");
    }

}
