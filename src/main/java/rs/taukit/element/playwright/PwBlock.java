package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import lombok.Setter;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IContainer;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static rs.taukit.utils.BasicUtils.scenarioStorage;

@Setter
@Getter
public class PwBlock extends PwElement implements IContainer {
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
        driver.waitForCondition(this::isVisible);
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
                } else if (iElement instanceof PwText) {
                    PwElement pwElement = iElement.castTo(PwElement.class);
                    Locator pwLocator = getElementPwLocator().locator(pwElement.elementPwLocator);
                    PwText pwText = ElementBuilder.buildElement(iElement.getElementName(), pwElement, pwLocator).castTo(PwText.class);
                    return pwText.getText().equals(text) ? pwText : null;
                }
            } catch (Exception e) {
                TauLogger.fail(e, "Не удалось найти элемент в контейнере '%s' по тексту '%s'", elementName, text);
            }
            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @Override
    public boolean containsElement(String elementName) {
        return descendants.containsKey(elementName);
    }

    @Override
    public Stream<IContainer> getStream() {
        return elementPwLocator.all().stream()
                .map(locator -> ElementBuilder.buildElement(elementName, this, locator))
                .map(locator -> locator.castTo(IContainer.class));
    }

    @Override
    public String getText() {
        return elementPwLocator.innerText();
    }

    @Override
    public boolean hasText(String expectedValue) {
        return getText().equals(expectedValue);
    }

    @Override
    public Boolean containsText(String expectedText) {
        return getText().contains(expectedText);
    }
}
