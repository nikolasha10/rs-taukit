package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.AElement;

@Getter
public class SimpleElement extends AElement {
    protected final String elementName;
    protected final Locator element;
    protected final String locator;

    public SimpleElement(String elementName, String locator) {
        PlaywrightDriver driver = (PlaywrightDriver) DriverManager.getInstance().getDriver();
        this.elementName = elementName;
        this.locator = locator;
        this.element = driver.getLocator(locator);
    }

    @Override
    public boolean isVisible() {
        return element.isVisible();
    }

    @Override
    public void click() {
        element.click();
    }

    public String getElementObject(){
        return locator;
    }
}
