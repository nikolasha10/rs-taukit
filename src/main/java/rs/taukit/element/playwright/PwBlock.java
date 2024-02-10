package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.cucumber.datatable.DataTable;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static rs.taukit.utils.BasicUtils.scenarioStorage;

public class PwBlock extends PwElement implements IHasBlockElements {
    protected final Map<String, IElement> elements;
    protected int currentIndex = 0;

    public PwBlock(String elementName, ElementModel model) {
        super(elementName, model);
        Map<String, String> elementMap = model.get("элементы");
        this.elements = ElementBuilder.buildElements(elementMap);
    }

    public PwBlock(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        this.model = model;
        this.locator = model.get("локатор");
        Map<String, String> elementMap = model.get("элементы");
        this.elements = ElementBuilder.buildElements(elementMap);
    }

    public PwBlock(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwBlock(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    @Override
    public List<IElement> getElementList() {
        return elements.values().stream().toList();
    }

    @Override
    public IElement getElementByName(int blockNumber, String elementName) {
        IElement iElement = Optional.ofNullable(elements.get(elementName))
                .orElseThrow(() -> {
                    TauLogger.console("В блоке \"%s\" описаны элементы: %s", this.elementName, elements.keySet());
                    return new ElementNotDescribedException(elementName, scenarioStorage().getCurrentPageName(), this);
                });
        PwElement pwElement = iElement.castTo(PwElement.class);
        try {
            BooleanSupplier condition = element.count() > blockNumber - 1
                    ? () -> element.all().get(blockNumber - 1).isVisible()
                    : () -> element.isVisible();
            driver.waitForCondition(condition);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться блока \"%s\" на странице \"%s\"",
                    this.elementName, scenarioStorage().getCurrentPageName());
        }

        Locator blockFieldLocator = element.all().get(blockNumber - 1).locator(pwElement.getElement());
        return ElementBuilder.buildElement(elementName, pwElement, blockFieldLocator);
    }

    @Override
    public IElement getElementByName(String elementName) {
        return isVisible() ? getElementByName(1, elementName) : null;
    }

    @Override
    public boolean containsElement(String elementName) {
        return elements.containsKey(elementName);
    }

    @Override
    public Stream<IHasBlockElements> getStream() {
        return element.all().stream()
                .map(locator -> ElementBuilder.buildElement(elementName, this, locator))
                .map(locator -> locator.castTo(IHasBlockElements.class));
    }

    public IHasBlockElements findByConditions(DataTable dataTable) {
        BooleanSupplier conditions = () -> dataTable.asMaps().stream()
                .map(data -> new ExpectedResult(data).waitForResult())
                .allMatch(ExpectedResult::isConditionMet);
        return null;
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }
}
