package rs.taukit.drivers;

import com.microsoft.playwright.*;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.context.TestContext;

@Getter
public class PlaywrightDriverOld implements WebDriver {
    public void openBrowser(String browserName) {
        TestContext testContext = TestContext.getInstance();
        Playwright playwright = Playwright.create();
        testContext.setPlaywright(playwright);
        playwright.selectors().setTestIdAttribute(Config.getProperty("autotest.element.id"));
        BrowserType browserType = getBrowserType(playwright, browserName);
        Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
        testContext.setBrowser(browser);
        BrowserContext context = browser.newContext();
        testContext.setContext(context);
        Page page = context.newPage();
        testContext.setPage(page);
    }

    public void reopenBrowser() {
        TestContext testContext = TestContext.getInstance();
        Browser browser = testContext.getBrowser();
        String browserName = browser.browserType().name();
        browser.close();
        testContext.setCurrentUser(null);
        openBrowser(browserName);
    }

    @Override
    public void close() {
        Playwright driver = TestContext.getInstance().getPlaywright();
        if (driver != null) {
            driver.close();
        }
    }

    private static BrowserType getBrowserType(Playwright playwright, String browserName) {
        switch (browserName) {
            case "chromium":
                return playwright.chromium();
            case "webkit":
                return playwright.webkit();
            case "firefox":
                return playwright.firefox();
            default:
                throw new IllegalArgumentException();
        }
    }
}
