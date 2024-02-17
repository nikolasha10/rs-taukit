package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import lombok.Setter;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.element.interfaces.IHasText;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static rs.taukit.utils.BasicUtils.scenarioStorage;

@Getter
public class PwBlock extends PwElement implements IHasBlockElements {
    @Setter
    private int currentIndex;

    public PwBlock(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwBlock(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwBlock(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwBlock(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    @Override
    public List<IElement> getElementList() {
        return descendants.values().stream().toList();
    }

    @Override
    public IElement getElementByName(String elementName) {
        try {
            driver.waitForCondition(this::isVisible);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться блока '%s' на странице '%s'",
                    this.elementName, scenarioStorage().getCurrentPageName());
        }
        if (!descendants.containsKey(elementName)) {
            throw new ElementNotDescribedException(elementName, scenarioStorage().getCurrentPageName(), this);
        }
        return descendants.get(elementName);
    }

    @Override
    public IElement getElementByText(String text) {
        if (!isVisible()) return null;
        return descendants.values().stream().map(iElement -> {
            try {
                if (iElement instanceof PwBlock pwBlock) {
                    return pwBlock.getElementByText(text);
                } else {
                    PwElement pwElement = iElement.castTo(PwElement.class);
                    Locator pwLocator = getElementPwLocator().locator(pwElement.elementPwLocator);
                    IHasText iHasText = ElementBuilder.buildElement(iElement.getElementName(), pwElement, pwLocator).castTo(IHasText.class);
                    return iHasText.getText().equals(text) ? iHasText : null;
                }
            } catch (Exception e) {
                TauLogger.fail(e, "какая-то фигня");
            }
            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @Override
    public boolean containsElement(String elementName) {
        return descendants.containsKey(elementName);
    }

    @Override
    public Stream<IHasBlockElements> getStream() {
        return elementPwLocator.all().stream()
                .map(locator -> ElementBuilder.buildElement(elementName, this, locator))
                .map(locator -> locator.castTo(IHasBlockElements.class));
    }
}
