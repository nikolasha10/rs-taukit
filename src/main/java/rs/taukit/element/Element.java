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
    protected final LinkedHashMap<String, String> elementData;
    public Element(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }
    public Element(String elementName, LinkedHashMap<String, String> elementData) {
        this.driver = DriverManager.getInstance().getDriver();
        this.elementName = elementName;
        this.elementData = elementData;
        this.locator = elementData.get("Локатор");
    }

    public <T> T castTo(Class<T> elementType){
        return (T) this;
    }
}
