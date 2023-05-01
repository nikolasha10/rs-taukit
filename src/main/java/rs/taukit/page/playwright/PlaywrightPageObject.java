package rs.taukit.page.playwright;

import com.microsoft.playwright.Page;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.element.ElementType;
import rs.taukit.element.IElement;
import rs.taukit.exceptions.ElementNotBuiltException;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.exceptions.PageFileNotFoundException;
import rs.taukit.exceptions.PageNotLoadException;
import rs.taukit.page.PageObject;
import rs.taukit.storage.StorageManager;
import rs.taukit.utils.TimeStringParser;
import rs.taukit.utils.YamlParser;

import java.nio.file.Paths;
import java.util.*;
import java.util.function.BooleanSupplier;

@Getter
public class PlaywrightPageObject implements PageObject {
    private final Page page;
    private final String pageName;
    private final String pageUrl;
    private final String pageControlUrl;
    private final Map<String, IElement> pageElements;
    private final List<String> expectedElements;
    private final Page.WaitForConditionOptions options;
    private final Page.ScreenshotOptions screenshotOptions;

    public PlaywrightPageObject(Page page, String pageName) {
        Map<String, ?> pageConfig = loadPageConfig(pageName);
        String pageTimeoutString = pageConfig.get("Максимальное время ожидания").toString();
        double pageTimeout = TimeStringParser.getTime(pageTimeoutString.equalsIgnoreCase("по умолчанию")
                ? Config.getProperty("pageLoadingTimeout") : pageTimeoutString);
        this.page = page;
        this.pageName = pageName;
        this.pageUrl = YamlParser.convert(pageConfig.get("Адрес"));
        this.pageControlUrl = YamlParser.convert(pageConfig.get("Обязательная часть адреса"));
        this.pageElements = buildElements(YamlParser.convert(pageConfig.get("Элементы")));
        this.expectedElements = YamlParser.convert(pageConfig.get("Обязательные элементы"));
        this.options = new Page.WaitForConditionOptions().setTimeout(pageTimeout);
        this.screenshotOptions = new Page.ScreenshotOptions()
                .setPath(Paths.get(pageName + ".png"))
                .setFullPage(true);
    }

//    private final Function<String, Locator> getElement = (elementName) -> {
//        Page page = TestContext.getInstance().getPage();
//        String locator = pageElements.get(elementName);
//        return locator.startsWith("//") ? page.locator(locator) : page.getByTestId(locator);
//    };

//    private final Function<String, List<Locator>> getListElement = (elementName) -> {
//        Map<String, String> list = YamlParser.convert(pageElements.get(elementName));
//        Locator items = getElement.apply(list.get("Элементы списка"));
//        Locator itemLabels = getElement.apply(list.get("Названия элементов"));
//        return Arrays.asList(items, itemLabels);
//    };

    public boolean isUrlCorrect() {
        return page.url().contains(pageControlUrl);
    }

    private boolean hasPageAllExpectedElements() {
        return expectedElements.stream().allMatch(elementName -> pageElements.get(elementName).isVisible());
    }

    private List<String> getAllNotFoundElements() {
        return expectedElements.stream()
                .filter(elementName -> !pageElements.get(elementName).isVisible())
                .map(elementName -> String.format("%s (%s)", elementName, pageElements.get(elementName).getLocator()))
                .toList();
    }

    public void waitForLoad() {
        try {
            BooleanSupplier waitCondition = () -> isUrlCorrect() && hasPageAllExpectedElements();
            page.waitForCondition(waitCondition, options);
            StorageManager.getInstance().getTestContext().setCurrentPageName(pageName);
        } catch (Exception e) {
            throw new PageNotLoadException(e, getPageNotLoadMessage());
        }
    }

    @Override
    public void waitForElement(String elementName, boolean isVisible) {
        page.waitForCondition(() -> pageElements.get(elementName).isVisible() == isVisible, options);
    }

    @Override
    public void waitForTimeout(double time) {
        page.waitForTimeout(time);
    }

    @Override
    public boolean isPageLoaded() {
        try {
            waitForLoad();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getPageNotLoadMessage() {
        String currentUrl = page.url();
        String urlNotContainsExpected = isUrlCorrect() ? "" : String.format(
                ". Текущий url (%s) не содержит ожидаемого текста (%s)",
                currentUrl, pageControlUrl);
        List<String> notFoundElements = getAllNotFoundElements();
        String elementsNotFound = notFoundElements.size() == 0 ? "" : String.format(
                ". Не найдены ожидаемые элементы: %s", String.join(", ", notFoundElements));
        return String.format("Не удалось дождаться отображения страницы '%s'%s%s",
                pageName, urlNotContainsExpected, elementsNotFound);
    }

    public void loadPage() {
        String url = Config.getSTAND().getUrl() + pageUrl;
        page.navigate(url);
        waitForLoad();
        StorageManager.getInstance().getTestContext().setCurrentPageName(pageName);
    }

    public byte[] takeScreenshot() {
        return page.screenshot(screenshotOptions);
    }

    private Map<String, ?> loadPageConfig(String pageName) {
        try {
            String pagesDirPath = Config.getProperty("pagesDirPath");
            String pageMappingFilePath = pagesDirPath + "/pageMapping.yaml";
            Map<String, String> pageMapping = YamlParser.parse(pageMappingFilePath);
            String pageFilePath = pagesDirPath + "/" + pageMapping.get(pageName);
            return YamlParser.parse(pageFilePath);
        } catch (Exception e) {
            throw new PageFileNotFoundException(e, pageName);
        }
    }

    private Map<String, IElement> buildElements(Map<String, ?> elements) {
        Map<String, IElement> result = new HashMap<>();
        elements.forEach((elementName, elementObject) -> {
            if (elementObject instanceof String) {
                result.put(elementName, buildElement(elementName, ElementType.SIMPLE_ELEMENT.getElementClass(), elementObject));
            } else {
                Map<String, String> object = YamlParser.convert(elementObject);
                Class<? extends IElement> elementClass = Arrays.stream(ElementType.values())
                        .filter(elementType -> elementType.getTypeName().equalsIgnoreCase(object.get("Тип")))
                        .findFirst().orElseThrow().getElementClass();
                result.put(elementName, buildElement(elementName, elementClass, object));
            }
        });
        return result;
    }

    private IElement buildElement(String elementName, Class<? extends IElement> elementClass, Object elementObject) {
        try {
            return elementClass.getConstructor(String.class, elementObject.getClass()).newInstance(elementName, elementObject);
        } catch (Exception e) {
            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), elementObject.toString());
        }

    }

    @Override
    public IElement getElement(String elementName) {
        return pageElements.get(elementName);
    }

    @Override
    public IElement replaceElement(String elementName, Class<? extends IElement> elementType) {
        IElement currentElement = Optional.ofNullable(pageElements.get(elementName))
                .orElseThrow(() -> new ElementNotDescribedException(elementName, pageName));
        pageElements.replace(elementName, buildElement(elementName, elementType, currentElement.getElementObject()));
        return pageElements.get(elementName);
    }
}
