package rs.taukit.driver.playwright;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.HarMode;
import com.microsoft.playwright.options.RouteFromHarUpdateContentPolicy;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import static rs.taukit.utils.BasicUtils.*;

@Getter
public class PlaywrightDriver implements IWebDriver {
    private final Playwright playwright = Playwright.create();
    private final List<PwBrowser> browserList = new ArrayList<>();
    private int currentBrowserIndex;

    public PlaywrightDriver() throws IOException {
        playwright.selectors().setTestIdAttribute(Config.getProperty("autotest.element.id"));
    }

    private BrowserType getBrowserType(String browserName) {
        return switch (browserName) {
            case "chromium" -> playwright.chromium();
            case "webkit" -> playwright.webkit();
            case "firefox" -> playwright.firefox();
            default -> throw new IllegalArgumentException();
        };
    }

    private BrowserContext getBrowserContext() {
        return getCurrentBrowser().getBrowserContext();
    }

    @Override
    public void openBrowser() {
        openBrowser(new ArrayList<>());
    }

    @Override
    public void openBrowser(List<String> args) {
        AtomicReference<Exception> suppressed = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            try {
                BrowserType browserType = getBrowserType(Objects.requireNonNull(Config.getProperty("browser.type")));
                Chromium chromium = new Chromium(browserType, browserList.size(), args);
                browserList.add(chromium);
                currentBrowserIndex = browserList.size() - 1;
                Boolean harFileSave = scenarioData().get("har-file.save");
                if (Config.getBooleanProperty("har-file.save") || harFileSave) {
                    chromium.context.routeFromHAR(
                            Path.of(getHarFilePath()),
                            new BrowserContext.RouteFromHAROptions()
                                    .setUpdate(true)
                                    .setUpdateContent(RouteFromHarUpdateContentPolicy.ATTACH)
                                    .setUpdateMode(HarMode.FULL));
                }
                return true;
            } catch (Exception e) {
                suppressed.set(e);
                return false;
            }
        };
        double interval = Config.getTimeProperty("condition.wait.timeout");
        double timeout = Config.getTimeProperty("conditions.wait.timeout");
        try {
            driver().waitForCondition(condition, interval, timeout);
        } catch (TimeoutError te) {
            throw new RuntimeException(suppressed.get());
        }
    }

    @Override
    public String getHarFilePath() {
        return String.format("%s/browser_%d/autotest.har", FileUtils.getDownloadFilePath(), currentBrowserIndex + 1);
    }

    @Override
    public void reopenBrowser() {
        closeBrowser();
        openBrowser();
    }

    @Override
    public void switchBrowser(int browserIndex) {
        this.currentBrowserIndex = browserIndex;
    }

    @Override
    public boolean isBrowserOpen() {
        return browserList.size() > currentBrowserIndex
                && getCurrentBrowser() != null
                && getCurrentBrowser().getBrowserContext() != null;
    }

    @Override
    public String getBrowserType() {
        return browserList.get(currentBrowserIndex).getType();
    }

    @Override
    public void closeBrowser() {
        getCurrentBrowser().close();
        getCurrentBrowser().removeChromeDir(currentBrowserIndex);
        scenarioStorage().setCurrentUser(null);
        browserList.remove(currentBrowserIndex);
    }

    @Override
    public void closeDriver() {
        if (isBrowserOpen()) {
            closeBrowser();
        }
        playwright.close();
    }

    @Override
    public void waitForCondition(BooleanSupplier condition) {
        double timeout = TimeStringParser.getTime(Objects.requireNonNull(Config.getProperty("condition.wait.timeout")));
        waitForCondition(condition, timeout);
    }

    @Override
    public void waitForCondition(BooleanSupplier condition, double timeout) {
        BrowserContext.WaitForConditionOptions setTimeout = new BrowserContext.WaitForConditionOptions().setTimeout(timeout);
        AtomicReference<Boolean> conditionIsMet = new AtomicReference<>(false);
        getBrowserContext().waitForCondition(() -> {
            if (!conditionIsMet.get()) {
                conditionIsMet.set(condition.getAsBoolean());
            }
            return conditionIsMet.get();
        }, setTimeout);
    }

    @Override
    public void waitForCondition(BooleanSupplier condition, double interval, double timeout) {
        long start = new Date().getTime();
        while (new Date().getTime() - start <= timeout && !condition.getAsBoolean()) {
            waitForTimeout(interval);
        }
    }

    @Override
    public void waitForCondition(BooleanSupplier condition, double timeout, List<Runnable> previousStepList) {
        String originalPageName = page() == null ? null : Objects.requireNonNull(page()).getPageName();
        IContainer originalContext = page() == null ? null : Objects.requireNonNull(page()).getCurrentContext();
        BrowserContext.WaitForConditionOptions setTimeout = new BrowserContext.WaitForConditionOptions().setTimeout(timeout);
        getBrowserContext().waitForCondition(() -> {
            try {
                if (!condition.getAsBoolean()) {
                    throw new RuntimeException();
                }
                return true;
            } catch (Exception e) {
                if (originalPageName != null) {
                    browser().getPage(originalPageName);
                    Objects.requireNonNull(page()).replaceCurrentContext(originalContext);
                }
                previousStepList.forEach(Runnable::run);
                return false;
            }
        }, setTimeout);
    }

    @Override
    public void waitForCondition(BooleanSupplier condition, double singleCheckTimeout, double timeout, List<Runnable> previousStepList) {
        double interval = previousStepList.isEmpty() ? timeout : singleCheckTimeout;
        BrowserContext.WaitForConditionOptions setTimeout = new BrowserContext.WaitForConditionOptions().setTimeout(timeout);
        getBrowserContext().waitForCondition(() -> {
            try {
                BrowserContext.WaitForConditionOptions setInterval = new BrowserContext.WaitForConditionOptions().setTimeout(interval);
                getBrowserContext().waitForCondition(condition, setInterval);
                return true;
            } catch (TimeoutError e) {
                previousStepList.forEach(Runnable::run);
                return false;
            } catch (AssertionError ae) {
                return condition.getAsBoolean();
            }
        }, setTimeout);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T castTo() {
        return (T) this;
    }

    public void waitForTimeout(double time) {
        try {
            Thread.sleep((long) time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PwBrowser getCurrentBrowser() {
        return browserList.isEmpty() ? null : browserList.get(currentBrowserIndex);
    }

    @Override
    public void closeOtherBrowsers(int activeBrowserIndex) {
        if (browserList.isEmpty()) return;
        int i = 0;
        while (browserList.size() > 1 && i < browserList.size()) {
            if (i == activeBrowserIndex) {
                i++;
            } else {
                TauLogger.console("Закрываем браузер %d", i);
                browserList.get(i).close();
                browserList.remove(i);
            }
            currentBrowserIndex = activeBrowserIndex;
        }
        TauLogger.console("Все браузеры кроме 1-го закрыты");
    }
}
