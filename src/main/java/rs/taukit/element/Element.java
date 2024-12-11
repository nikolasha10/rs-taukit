package rs.taukit.element;

import lombok.Getter;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.exceptions.ElementNotBuiltException;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.utils.TestDataUtils;

import java.util.*;

@Getter
@SuppressWarnings("unchecked")
public abstract class Element implements IElement {
    protected final String elementName;
    protected String searchMethodName;
    protected String selector;
    protected ElementModel model;
    protected Map<String, IElement> descendants = new HashMap<>();

    public Element(String elementName, String selector) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", selector)));
    }

    public Element(String elementName, LinkedHashMap<String, Object> elementData) {
        this.elementName = elementName;
        setModel(new ElementModel(elementData));
    }

    public Element(String elementName, ElementModel model) {
        this.elementName = elementName;
        setModel(model);
    }

    public Element(String elementName, IElement element) {
        this.elementName = elementName;
        setModel(element.getModel());
    }

    public <T extends IElement> T getDescendant(String descendantName) {
        return (T) TestDataUtils.get(descendants, descendantName);
    }

    public void setModel(ElementModel model) {
        this.model = model;
        List<String> searchOptions = Optional.ofNullable(model.get(Arrays.asList("test-id", "xpath")))
                .orElseThrow(ElementNotBuiltException.searchMethodNotFound(elementName, model));
        this.searchMethodName = searchOptions.get(0);
        this.selector = searchOptions.get(1);
        Map<String, Map<String, Object>> elements = model.get("элементы", false);
        ElementBuilder.buildElements(elements, this.descendants);
    }

    public <T> T castTo(Class<T> elementType) {
        return (T) this;
    }

}
