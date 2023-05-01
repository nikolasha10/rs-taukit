package listeners;

import org.slf4j.MDC;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;
import rs.taukit.config.Config;
import rs.taukit.driver.DriverManager;

public class SuiteListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
        Config.init("src/test/efr/resources/configs");
        XmlSuite xmlSuite = suite.getXmlSuite();
        int threadCount = Integer.parseInt(Config.getProperty("threadCount"));
        xmlSuite.setDataProviderThreadCount(threadCount);
    }

    @Override
    public void onFinish(ISuite suite) {
        DriverManager.getInstance().closeDrivers();
        MDC.remove("logName");
    }
}
