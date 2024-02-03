package rs.taukit.element;

import lombok.Getter;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.IElement;

import java.util.LinkedHashMap;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.driver;

@Getter
@SuppressWarnings("unchecked")
public abstract class Element implements IElement {
    protected final String elementName;
    protected final String locator;
    protected final IWebDriver driver = driver();
    protected final ElementModel model;

    public Element(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }

    public Element(String elementName, LinkedHashMap<String, Object> elementData) {
        this.elementName = elementName;
        this.model = new ElementModel(elementData);
        this.locator = model.get("локатор");
    }

    public Element(String elementName, ElementModel model) {
        this.elementName = elementName;
        this.model = model;
        this.locator = model.get("локатор");
    }

    public Element(String elementName, IElement element) {
        this.elementName = elementName;
        this.model = element.getModel();
        this.locator = model.get("локатор");
    }

    public <T> T castTo(Class<T> elementType) {
        return (T) this;
    }
}
