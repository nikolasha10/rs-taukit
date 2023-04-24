package rs.taukit.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.context.TestContext;
import rs.taukit.exceptions.PageNotLoadException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

@Getter
public class PageObject {
    private final String pageName;
    private final String pageUrl;
    private final String pageControlUrl;
    private Map<String, String> pageElements;
    private final List<String> expectedElements;
    private final Page.WaitForConditionOptions options;
    private final Page.ScreenshotOptions screenshotOptions;

    public PageObject(
            String pageName,
            String pageUrl,
            String pageControlUrl,
            Map<String, String> pageElements,
            List<String> expectedElements,
            Page.WaitForConditionOptions options,
            Page.ScreenshotOptions screenshotOptions) {
        this.pageName = pageName;
        this.pageUrl = pageUrl;
        this.pageControlUrl = pageControlUrl;
        this.pageElements = pageElements;
        this.expectedElements = expectedElements;
        this.options = options;
        this.screenshotOptions = screenshotOptions;
    }

    private final Function<String, Locator> getElement = (elementName) -> {
        Page page = TestContext.getInstance().getPage();
        String locator = pageElements.get(elementName);
        return locator.startsWith("//") ? page.locator(locator) : page.getByTestId(locator);
    };

    private boolean isUrlCorrect() {
        Page page = TestContext.getInstance().getPage();
        return page.url().contains(pageControlUrl);
    }

    private boolean hasPageAllExpectedElements() {
        return expectedElements.stream().allMatch(elementName -> getElement.apply(elementName).isVisible());
    }

    private List<String> getAllNotFoundElements() {
        return expectedElements.stream()
                .filter(elementName -> !getElement.apply(elementName).isVisible())
                .map(elementName -> String.format("%s (%s)", elementName, getElement.apply(elementName))).toList();
    }

    public void waitForLoad() {
        try {
            Page page = TestContext.getInstance().getPage();
            BooleanSupplier waitCondition = () -> isUrlCorrect() && hasPageAllExpectedElements();
            page.waitForCondition(waitCondition, options);
            TestContext.getInstance().setCurrentPageName(pageName);
        } catch (Exception e) {
            throw new PageNotLoadException(e, isUrlCorrect(), getAllNotFoundElements());
        }
    }

    public void loadPage() {
        Page page = TestContext.getInstance().getPage();
        String url = Config.getSTAND().getUrl() + pageUrl;
        page.navigate(url);
        waitForLoad();
        TestContext.getInstance().setCurrentPageName(pageName);
    }

    public String fill(String fieldName, String value) {
        getElement.apply(fieldName).fill(value);
        getElement.apply(fieldName).blur();
        return getElement.apply(fieldName).inputValue();
    }

    public void click(String elementName) {
        getElement.apply(elementName).click();
    }

    public byte[] takeScreenshot() {
        Page page = TestContext.getInstance().getPage();
        return page.screenshot(screenshotOptions);
    }
}
