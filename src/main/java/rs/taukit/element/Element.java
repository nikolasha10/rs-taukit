package rs.taukit.element;

import lombok.Getter;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.WebDriver;
import rs.taukit.element.interfaces.IElement;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@SuppressWarnings("unchecked")
public abstract class Element implements IElement {
    protected final String elementName;
    protected final String locator;
    protected final WebDriver driver;
    protected final ElementModel model;

    public Element(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }

    public Element(String elementName, LinkedHashMap<String, Object> elementData) {
        this.driver = DriverManager.getInstance().getDriver();
        this.elementName = elementName;
        this.model = new ElementModel(elementData);
        this.locator = model.get("локатор");
    }

    public Element(String elementName, ElementModel model) {
        this.driver = DriverManager.getInstance().getDriver();
        this.elementName = elementName;
        this.model = model;
        this.locator = model.get("локатор");
    }

    public Element(String elementName, IElement element) {
        this.driver = DriverManager.getInstance().getDriver();
        this.elementName = elementName;
        this.model = element.getModel();
        this.locator = model.get("локатор");
    }

    public <T> T castTo(Class<T> elementType) {
        return (T) this;
    }
}
