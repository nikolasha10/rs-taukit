package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.page.playwright.ElementBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static rs.taukit.utils.BasicUtils.testContext;

public class PwBlock extends PwElement implements IHasBlockElements {
    protected final Map<String, IElement> elements;

    public PwBlock(String elementName, ElementModel model) {
        super(elementName, model);
        Map<String, String> elementMap = model.get("элементы");
        this.elements = ElementBuilder.buildElements(elementMap);
    }

    public PwBlock(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        Map<String, String> elementMap = model.get("элементы");
        this.elements = ElementBuilder.buildElements(elementMap);
    }

    public PwBlock(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    @Override
    public IElement getElementByName(int blockNumber, String elementName) {
        IElement iElement = Optional.ofNullable(elements.get(elementName))
                .orElseThrow(() -> {
                    System.out.println(elements.keySet());
                    return new ElementNotDescribedException(elementName, testContext().getCurrentPageName());
                });
        PwElement pwElement = iElement.castTo(PwElement.class);
        Locator blockFieldLocator = element.all().get(blockNumber - 1).locator(pwElement.getElement());
        return ElementBuilder.buildElement(elementName, pwElement, blockFieldLocator);
    }
}
