package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.AElement;
import rs.taukit.element.IHasAttachment;

import java.util.LinkedHashMap;

public class FileAttachment extends AElement implements IHasAttachment {
    private final String elementName;
    private final Locator element;
    private final String locator;
    @Getter
    private final LinkedHashMap<String, String> elementObject;

    public FileAttachment(String elementName, LinkedHashMap<String, String> elementObject) {
        PlaywrightDriver driver = (PlaywrightDriver) DriverManager.getInstance().getDriver();
        this.elementObject = elementObject;
        this.elementName = elementName;
        this.locator = elementObject.get("Локатор");
        this.element = driver.getLocator(locator);
    }

    @Override
    public String getLocator() {
        return locator;
    }

    @Override
    public boolean isVisible() {
        return element.isVisible();
    }

    @Override
    public void click() {
        element.click();
    }

}
