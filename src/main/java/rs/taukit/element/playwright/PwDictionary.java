package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.page.PageObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.browser;
import static rs.taukit.utils.BasicUtils.page;

@Getter
public class PwDictionary extends PwElement implements Editable {
    public PwDictionary(String elementName, Locator locator) {
        super(elementName, "");
    }

    public PwDictionary(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }

    public PwDictionary(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwDictionary(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwDictionary(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwDictionary(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public void setValue(String value) {
        PageObject initialPage = page();
        descendants.get("открыть справочник").castTo(PwButton.class).click();
        page().waitForPopup(model.get("страница"));
        page().getElement(model.get("поле поиска").toString()).castTo(PwTextField.class).setValue(value);
        page().getElement(model.get("кнопка поиска").toString()).castTo(PwButton.class).click();
        driver.waitForTimeout(2000);
        page().getElement("Значения справочника").castTo(PwList.class).setValue(value);
        page().waitForPopupClose(model.get("страница"));
        browser().getPage(initialPage.getPageName()).waitForLoad();
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public boolean isVisible() {
        return elementPwLocator.isVisible();
    }

    @Override
    public void click() {
        elementPwLocator.click();
    }

    @Override
    public String getText() {
        return elementPwLocator.innerText();
    }

    @Override
    public boolean hasText(String expectedValue) {
        return false;
    }

    @Override
    public Boolean containsText(String expectedText) {
        return null;
    }
}
