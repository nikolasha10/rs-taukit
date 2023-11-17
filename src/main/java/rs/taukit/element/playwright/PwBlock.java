package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.cucumber.datatable.DataTable;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.utils.YamlParser;

import java.util.LinkedHashMap;
import java.util.Map;

public class PwBlock extends PwElement implements IHasBlockElements {
    protected final Map<String, IElement> elements;

    public PwBlock(String elementName, ElementModel model) {
        super(elementName, model);
        Map<String, String> elementMap = model.get("элементы");
        this.elements = ElementBuilder.buildElements(elementMap);
    }

    public PwBlock(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    @Override
    public IElement getElementByName(int blockNumber, String elementName) {
        PwElement pwElement = elements.get(elementName).castTo(PwElement.class);
        Locator blockFieldLocator = element.all().get(blockNumber-1).locator(pwElement.getElement());
        return ElementBuilder.buildElement(elementName, pwElement, blockFieldLocator);
    }
}
