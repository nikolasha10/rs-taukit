package rs.taukit.drivers;

import com.microsoft.playwright.*;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.context.TestContext;

@Getter
public class PlaywrightDriver implements WebDriver {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;

    public void openBrowser(String browserName) {
        playwright = Playwright.create();
        playwright.selectors().setTestIdAttribute(Config.getProperty("autotest.element.id"));
        BrowserType browserType = getBrowserType(playwright, browserName);
        browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
        browserContext = browser.newContext();
        page = browserContext.newPage();

        TestContext testContext = TestContext.getInstance();
        testContext.setPlaywright(playwright);
        testContext.setBrowser(browser);
        testContext.setContext(browserContext);
        testContext.setPage(page);
    }

    public void reopenBrowser() {
        TestContext testContext = TestContext.getInstance();
        String browserName = browser.browserType().name();
        close();
        testContext.setCurrentUser(null);
        openBrowser(browserName);
    }

    @Override
    public void close() {
        if (playwright != null) {
            playwright.close();
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
