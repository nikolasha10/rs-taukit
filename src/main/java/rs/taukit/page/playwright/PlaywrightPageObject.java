package rs.taukit.page.playwright;

import com.microsoft.playwright.Page;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.playwright.PwElement;
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

import static rs.taukit.utils.TestDataUtils.loadPageConfig;

@Getter
public class PlaywrightPageObject implements PageObject {
    private final Page page;
    private final String pageName;
    private final String pageUrl;
    private final String pageControlUrl;
    private final Map<String, IElement> pageElements = new HashMap<>();
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
        this.pageElements.putAll(buildElements(YamlParser.convert(pageConfig.get("Элементы"))));
        List<String> expectedElements = YamlParser.convert(pageConfig.get("Обязательные элементы"));
        this.expectedElements = Optional.ofNullable(expectedElements).orElse(new ArrayList<>());
        this.options = new Page.WaitForConditionOptions().setTimeout(pageTimeout);
        this.screenshotOptions = new Page.ScreenshotOptions()
                .setPath(Paths.get(pageName + ".png"))
                .setFullPage(true);
    }

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
        IElement element = Optional.ofNullable(pageElements.get(elementName))
                .orElseThrow(() -> new ElementNotDescribedException(elementName, pageName));
        page.waitForCondition(() -> element.isVisible() == isVisible, options);
    }

    @Override
    public void waitForPopup(Runnable runnable) {
        page.waitForPopup(runnable);
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
        String url = pageUrl.startsWith("file") ? pageUrl : Config.getSTAND().getUrl() + pageUrl;
        try {
            page.navigate(url);
        } catch (Exception e){

        }
        waitForLoad();
        StorageManager.getInstance().getTestContext().setCurrentPageName(pageName);
    }

    public byte[] takeScreenshot() {
        return page.screenshot((new Page.ScreenshotOptions().setFullPage(true)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, IElement> buildElements(Map<String, ?> elements) {
        Map<String, IElement> result = new HashMap<>();
        elements.forEach((elementName, elementObject) -> {
            if (elementObject instanceof String locator){
                result.put(elementName, buildElement(elementName, locator));
            } else if (elementObject instanceof Map elementData){
                IElement element = buildElement(elementName, elementData);
                if (element != null) result.put(elementName, element);
            }
        });
        return result;
    }

    private IElement buildElement(String elementName, String locator) {
        return new PwElement(elementName, locator);
    }

    private IElement buildElement(String elementName, Map<String, String> elementData) {
        String elementTypeName = elementData.get("Тип");
        if (elementTypeName.equals("общий блок")) {
            return addBlock(elementData);
        }
        Class<? extends IElement> elementClass = ElementType.getType(elementTypeName).getElementClass();
        return buildElement(elementName, elementClass, elementData);
    }
    private IElement buildElement(String elementName, Class<? extends IElement> elementClass, Map<String, String> elementData) {
        try {
            return elementClass.getConstructor(String.class, elementData.getClass())
                    .newInstance(elementName, elementData);
        } catch (Exception e) {
            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), elementData.toString());
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
        pageElements.replace(elementName, buildElement(elementName, elementType, currentElement.getElementData()));
        return pageElements.get(elementName);
    }

    public IElement addBlock(Map<String, String> blockData) {
        String blockDescription = blockData.get("Описание");
        String filePath = blockData.get("Файл");
        List<Map<String, ?>> commonBlocksData = YamlParser.parse(filePath);
        Map<String, ?> blockElementsData = YamlParser.convert(commonBlocksData.stream()
                .filter(data -> data.get("Описание").equals(blockDescription)).findFirst().orElseThrow()
                .get("Элементы"));
        pageElements.putAll(buildElements(blockElementsData));
        return null;
    }
}
