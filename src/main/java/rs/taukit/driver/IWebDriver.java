package rs.taukit.driver;

import java.util.List;
import java.util.function.BooleanSupplier;

public interface IWebDriver {
    void openBrowser();

    void openBrowser(boolean viewPdf);

    void reopenBrowser();

    void switchBrowser(int browserIndex);

    boolean isBrowserOpen();

    String getBrowserType();

    int getCurrentBrowserIndex();

    void closeDriver();

    void closeBrowser();

    void waitForCondition(BooleanSupplier condition);

    void waitForCondition(BooleanSupplier condition, double searchValueTimeout);

    void waitForCondition(BooleanSupplier condition, double interval, double timeout);

    void waitForCondition(BooleanSupplier condition, double interval, double timeout, List<Runnable> previousStep);

    <T> T castTo();

    void waitForTimeout(double time);

    IWebBrowser getCurrentBrowser();

    void closeOtherBrowsers(int activeBrowserIndex);
}
