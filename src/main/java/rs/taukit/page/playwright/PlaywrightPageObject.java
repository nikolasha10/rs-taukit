package rs.taukit.page.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.element.playwright.PwBlock;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.exceptions.PageFragmentNotFoundException;
import rs.taukit.exceptions.PageNotClosedException;
import rs.taukit.exceptions.PageNotLoadException;
import rs.taukit.page.PageObject;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TimeStringParser;
import rs.taukit.utils.YamlParser;

import java.nio.file.Paths;
import java.util.*;
import java.util.function.BooleanSupplier;

import static rs.taukit.utils.BasicUtils.scenarioStorage;
import static rs.taukit.utils.TestDataUtils.loadPageConfig;

@Getter
public class PlaywrightPageObject implements PageObject {
    private final Page page;
    private final String pageName;
    private final String pageUrl;
    private final String pageControlUrl;
    private final PwBlock container;
    private final List<String> expectedElements;
    private final Page.ScreenshotOptions screenshotOptions;
    private final Deque<IHasBlockElements> currentContext = new LinkedList<>();
    private PlaywrightPageObject popup;
    private Page.WaitForConditionOptions options;
    private double pageTimeout;
    private Exception thrownException;

    public PlaywrightPageObject(Page page, String pageName) {
        Map<String, ?> pageConfig = loadPageConfig(pageName);
        String pageTimeoutString = pageConfig.get("Максимальное время ожидания").toString();
        setPageTimeout(pageTimeoutString.equalsIgnoreCase("по умолчанию")
                ? Config.getProperty("pageLoadingTimeout") : pageTimeoutString);
        this.page = page;
        this.pageName = pageName;
        String pageUrl = YamlParser.convert(pageConfig.get("Адрес"));
        this.pageUrl = Config.getSTAND().getUrl() + Optional.ofNullable(pageUrl).orElse("");
        String controlUrl = YamlParser.convert(pageConfig.get("Обязательная часть адреса"));
        this.pageControlUrl = Optional.ofNullable(controlUrl).orElse("");
        Map<String, Map<String, Object>> elementList = YamlParser.convert(pageConfig.get("Элементы"));
        if (elementList == null) {
            elementList = new HashMap<>();
        }
        List<String> commonBlockList = YamlParser.convert(pageConfig.get("Общие блоки элементов"));
        elementList.putAll(addCommonBlocks(commonBlockList));
        Map<String, Object> bodyStructure = Map.of(
                "тип", "контейнер",
                "xpath", "//body",
                "элементы", elementList);
        ElementModel bodyModel = new ElementModel(bodyStructure);
        this.container = ElementBuilder.buildElement(pageName, PwBlock.class, bodyModel).castTo(PwBlock.class);
        this.currentContext.add(this.container);
        List<String> expectedElements = YamlParser.convert(pageConfig.get("Обязательные элементы"));
        this.expectedElements = Optional.ofNullable(expectedElements).orElse(new ArrayList<>());
        this.options = new Page.WaitForConditionOptions().setTimeout(pageTimeout);
        this.screenshotOptions = new Page.ScreenshotOptions()
                .setPath(Paths.get(pageName + ".png"))
                .setFullPage(true);
    }

    public String getCurrentUrl(){
        return page.url();
    }
    public boolean isUrlCorrect() {
        return page.url().contains(pageControlUrl);
    }

    private boolean hasPageAllExpectedElements() {
        try {
            List<String> notDescribed = expectedElements.stream()
                    .filter(elementName -> container.getElementByName(elementName) == null)
                    .toList();
            if (!notDescribed.isEmpty()) {
                throw new ElementNotDescribedException(notDescribed, pageName);
            }
            return expectedElements.stream().allMatch(elementName -> container.getElementByName(elementName).isVisible());
        } catch (Exception e){
            TauLogger.console(e);
            return false;
        }
    }

    private List<String> getAllNotFoundElements() {
        return expectedElements.stream()
                .filter(elementName -> container.getElementByName(elementName) != null && !container.getElementByName(elementName).isVisible())
                .map(elementName -> String.format("%s (%s)", elementName, container.getElementByName(elementName).getSelector()))
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
        waitForLoadAndContinue(pageTimeout);
    }

    public void waitForLoadAndContinue(double pageTimeout) {
        BooleanSupplier waitCondition = () -> isUrlCorrect() && hasPageAllExpectedElements();
        BasicUtils.driver().waitForCondition(waitCondition, pageTimeout);
        scenarioStorage().setCurrentPageName(pageName);
    }

    public void waitForClose() {
        try {
            BooleanSupplier waitCondition = () -> !isUrlCorrect() || !hasPageAllExpectedElements();
            page.waitForCondition(waitCondition, options);
        } catch (Exception e) {
            throw new PageNotClosedException(e, String.format("Не удалось дождаться закрытия страницы \"%s\"", pageName));
        }

    }

    @Override
    public void waitForElement(String elementName, boolean isVisible) {
        IHasBlockElements context = getCurrentContext();
        IElement expectedElement = context.getElementByName(elementName);
        IElement element = Optional.ofNullable(expectedElement)
                .orElseThrow(() -> new ElementNotDescribedException(elementName, pageName));
        page.waitForCondition(() -> element.isVisible() == isVisible, options);
    }

    @Override
    public void waitForPopup(String pageName) {
        this.popup = new PlaywrightPageObject(page, pageName);
        popup.waitForLoad();
        this.currentContext.add(popup.getCurrentContext());
    }

    @Override
    public void waitForPopupClose(String pageName) {
        popup.waitForClose();
        dropCurrentContext();
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
    public void setCurrentContext(Object blockElement) {

        switch (blockElement) {
            case null -> {
                currentContext.clear();
                currentContext.add(container);
            }
            case String blockName -> {
                try {
                    this.currentContext.add(getElement(blockName).castTo(IHasBlockElements.class));
                } catch (Exception e) {
                    String message = String.format("Не удалось установить элемент '%s' (%s) в качестве текущего контекста",
                            blockName, getElement(blockName).getModel().getType());
                    throw new RuntimeException(message, e);
                }
            }
            case IHasBlockElements iHasBlockElements -> this.currentContext.add(iHasBlockElements);
            default -> {
            }
        }

    }

    @Override
    public void replaceCurrentContext(IHasBlockElements blockElements) {
        dropCurrentContext();
        this.currentContext.add(blockElements);
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
        return String.format("Не удалось дождаться отображения страницы \"%s\"%s%s",
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
        scenarioStorage().setCurrentPageName(pageName);
    }

    public void reload() {
        page.reload();
        waitForLoad();
    }

    public byte[] takeScreenshot() {
        return page.screenshot((new Page.ScreenshotOptions().setFullPage(true)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IElement> T getElement(String elementName) {
        try {
            BasicUtils.driver().waitForCondition(() -> findElement(getCurrentContext(), elementName) != null);
        } catch (Exception ignored) {
            // ignored
        }
        return (T) Optional.ofNullable(findElement(getCurrentContext(), elementName))
                .orElseThrow(() -> new ElementNotDescribedException(elementName, pageName, getCurrentContext()));
    }

    private IElement findElement(IHasBlockElements context, String elementName) {
        List<IElement> elementList;
        if (context.containsElement(elementName)) {
            return context.getElementByName(elementName);
        }
        elementList = context.getElementList();
        List<IHasBlockElements> blockElementsList = elementList.stream()
                .filter(element -> element.getModel().getType().equalsIgnoreCase("блок элементов"))
                .map(element -> element.castTo(IHasBlockElements.class))
                .toList();
        for (IHasBlockElements blockElement : blockElementsList) {
            if (context.containsElement(blockElement.getElementName())) {
                return null;
            }
            IHasBlockElements newContext = context.getElementByName(blockElement.getElementName()).castTo(IHasBlockElements.class);
            IElement foundElement = findElement(newContext, elementName);
            if (foundElement != null) return foundElement;
        }
        return null;
    }

    public Map<String, Map<String, Object>> addCommonBlocks(List<String> blockNameList) {
        Map<String, Map<String, Object>> blockElementsData = new HashMap<>();
        Optional.ofNullable(blockNameList).orElse(new ArrayList<>()).forEach(blockName -> {
            String filePath = Optional.ofNullable(Config.getFragmentFilePath(blockName))
                    .orElseThrow(() -> new PageFragmentNotFoundException(blockName));
            Map<String, ?> blockData = YamlParser.parse(filePath);
            blockElementsData.putAll(YamlParser.convert(blockData.get("Элементы")));
        });
        return blockElementsData;
    }

    @Override
    public void setPageTimeout(String timeoutString) {
        this.pageTimeout = TimeStringParser.getTime(timeoutString);
    }

    @Override
    public void setPageTimeout(double timeout) {
        this.pageTimeout = timeout;
    }

    @Override
    public void close() {
        page.close();
    }

    @Override
    public void setActive() {
        page.bringToFront();
    }

    public Locator locator(String selector) {
        return page.locator(selector);
    }

    public Locator getByTestId(String testId) {
        return page.getByTestId(testId);
    }
}
