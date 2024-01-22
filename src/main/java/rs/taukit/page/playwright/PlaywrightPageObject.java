package rs.taukit.page.playwright;

import com.microsoft.playwright.Page;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.exceptions.PageFragmentNotFoundException;
import rs.taukit.exceptions.PageNotClosedException;
import rs.taukit.exceptions.PageNotLoadException;
import rs.taukit.page.PageObject;
import rs.taukit.storage.StorageManager;
import rs.taukit.utils.TimeStringParser;
import rs.taukit.utils.YamlParser;

import java.nio.file.Paths;
import java.util.*;
import java.util.function.BooleanSupplier;

import static rs.taukit.utils.BasicUtils.driver;
import static rs.taukit.utils.TestDataUtils.loadPageConfig;

@Getter
public class PlaywrightPageObject implements PageObject {
    private final Page page;
    private final String pageName;
    private final String pageUrl;
    private final String pageControlUrl;
    private final Map<String, IElement> pageElements = new HashMap<>();
    private final List<String> expectedElements;
    private final Page.ScreenshotOptions screenshotOptions;
    private final Deque<IHasBlockElements> currentContext = new LinkedList<>();
    private int currentTabIndex;
    private Page.WaitForConditionOptions options;
    private double pageTimeout;
    private Exception thrownException;

    public PlaywrightPageObject(Page page, String pageName) {
        Map<String, ?> pageConfig = loadPageConfig(pageName);
        String pageTimeoutString = pageConfig.get("Максимальное время ожидания").toString();
        setPageTimeout(pageTimeoutString.equalsIgnoreCase("по умолчанию")
                ? Config.getProperty("pageLoadingTimeout") : pageTimeoutString);
        this.page = page;
        this.currentTabIndex = driver().getPageIndex();
        this.pageName = pageName;
        String pageUrl = YamlParser.convert(pageConfig.get("Адрес"));
        this.pageUrl = Config.getSTAND().getUrl() + Optional.ofNullable(pageUrl).orElse("");
        String controlUrl = YamlParser.convert(pageConfig.get("Обязательная часть адреса"));
        this.pageControlUrl = Optional.ofNullable(controlUrl).orElse("");
        List<String> commonBlockList = YamlParser.convert(pageConfig.get("Общие блоки элементов"));
        if (commonBlockList != null) {
            this.pageElements.putAll(addCommonBlocks(commonBlockList));
        }
        Map<String, ?> elementList = YamlParser.convert(pageConfig.get("Элементы"));
        if (elementList != null) {
            this.pageElements.putAll(ElementBuilder.buildElements(elementList));
        }
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
        List<String> notDescribed = expectedElements.stream()
                .filter(elementName -> pageElements.get(elementName) == null)
                .toList();
        if (!notDescribed.isEmpty()) {
            throw new ElementNotDescribedException(notDescribed, pageName);
        }
        return expectedElements.stream().allMatch(elementName -> pageElements.get(elementName).isVisible());
    }

    private List<String> getAllNotFoundElements() {
        return expectedElements.stream()
                .filter(elementName -> pageElements.get(elementName) != null && !pageElements.get(elementName).isVisible())
                .map(elementName -> String.format("%s (%s)", elementName, pageElements.get(elementName).getLocator()))
                .toList();
    }

    public void waitForLoad() {
        try {
            waitForLoadAndContinue();
        } catch (Exception e) {
            throw new PageNotLoadException(e, getPageNotLoadMessage());
        }
    }

    public void waitForLoadAndContinue() {
        BooleanSupplier waitCondition = () -> isUrlCorrect() && hasPageAllExpectedElements();
        this.options = new Page.WaitForConditionOptions().setTimeout(pageTimeout);
        page.waitForCondition(waitCondition, options);
        StorageManager.getInstance().getTestContext().setCurrentPageName(pageName);
    }

    public void waitForClose() {
        try {
            BooleanSupplier waitCondition = () -> !isUrlCorrect() || !hasPageAllExpectedElements();
            page.waitForCondition(waitCondition, options);
        } catch (Exception e) {
            throw new PageNotClosedException(e, String.format("Не удалось дождаться закрытия страницы '%s'", pageName));
        }

    }

    @Override
    public void waitForElement(String elementName, boolean isVisible) {
        IHasBlockElements context = getCurrentContext();
        IElement expectedElement = context == null ? pageElements.get(elementName) : context.getElementByName(elementName);
        IElement element = Optional.ofNullable(expectedElement)
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
            waitForLoadAndContinue();
            return true;
        } catch (Exception e) {
            this.thrownException = e;
            return false;
        }
    }

    @Override
    public boolean isPageClosed() {
        try {
            waitForClose();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public IHasBlockElements getCurrentContext() {
        return this.currentContext.peekLast();
    }

    @Override
    public void setCurrentContext(String blockName) {
        if (blockName == null) {
            this.currentContext.clear();
        } else {
            this.currentContext.add(getElement(blockName).castTo(IHasBlockElements.class));
        }
    }

    @Override
    public IHasBlockElements dropCurrentContext() {
        this.currentContext.pollLast();
        return this.currentContext.peekLast();
    }

    private String getPageNotLoadMessage() {
        String currentUrl = page.url();
        String urlNotContainsExpected = isUrlCorrect() ? "" : String.format(
                ". Текущий url (%s) не содержит ожидаемого текста (%s)",
                currentUrl, pageControlUrl);
        List<String> notFoundElements = getAllNotFoundElements();
        String elementsNotFound = notFoundElements.isEmpty() ? "" : String.format(
                ". Не найдены ожидаемые элементы: %s", String.join(", ", notFoundElements));
        return String.format("Не удалось дождаться отображения страницы '%s'%s%s",
                pageName, urlNotContainsExpected, elementsNotFound);
    }

    public void loadPage() {
        String url = pageUrl.startsWith("file") ? pageUrl : Config.getSTAND().getUrl() + pageUrl;
        try {
            page.navigate(url);
        } catch (Exception ignored) {
            // ignored
        }
        waitForLoad();
        StorageManager.getInstance().getTestContext().setCurrentPageName(pageName);
    }

    public void reload() {
        page.reload();
        waitForLoad();
    }

    public byte[] takeScreenshot() {
        return page.screenshot((new Page.ScreenshotOptions().setFullPage(true)));
    }

    @Override
    public IElement getElement(String elementName) {
        return Optional.ofNullable(findElement(getCurrentContext(), elementName))
                .orElseThrow(() -> new ElementNotDescribedException(elementName, pageName));
    }

    private IElement findElement(IHasBlockElements context, String elementName) {
        List<IElement> elementList;
        if (context == null) {
            if (pageElements.containsKey(elementName)) {
                return pageElements.get(elementName);
            }
            elementList = pageElements.values().stream().toList();
        } else {
            if (context.containsElement(elementName)) {
                return context.getElementByName(elementName);
            }
            elementList = context.getElementList();
        }
        List<IHasBlockElements> blockElementsList = elementList.stream()
                .filter(element -> element.getModel().getType().equalsIgnoreCase("блок элементов"))
                .map(element -> element.castTo(IHasBlockElements.class))
                .toList();
        for (IHasBlockElements blockElement : blockElementsList) {
            IElement foundElement = findElement(blockElement, elementName);
            if (foundElement != null) return foundElement;
        }
        return null;
    }

    @Override
    public IElement replaceElement(String elementName, Class<? extends IElement> elementType) {
        IElement currentElement = Optional.ofNullable(findElement(getCurrentContext(), elementName))
                .orElseThrow(() -> new ElementNotDescribedException(elementName, pageName));
        if (getCurrentContext() == null && pageElements.containsKey(elementName)) {
            pageElements.replace(elementName, ElementBuilder.buildElement(elementName, elementType, currentElement));
            return pageElements.get(elementName);
        } else {
            return currentElement.castTo(elementType);
        }
    }

    public Map<String, IElement> addCommonBlocks(List<String> blockNameList) {
        Map<String, IElement> result = new HashMap<>();
        blockNameList.forEach(blockName -> {
            String filePath = Optional.ofNullable(Config.getFragmentFilePath(blockName))
                    .orElseThrow(() -> new PageFragmentNotFoundException(blockName));
            Map<String, ?> blockData = YamlParser.parse(filePath);
            Map<String, ?> blockElementsData = YamlParser.convert(blockData.get("Элементы"));
            result.putAll(ElementBuilder.buildElements(blockElementsData));
        });
        return result;
    }

    @Override
    public void setPageTimeout(String timeoutString) {
        this.pageTimeout = TimeStringParser.getTime(timeoutString);
    }

    @Override
    public void setPageTimeout(double timeout) {
        this.pageTimeout = timeout;
    }
}
