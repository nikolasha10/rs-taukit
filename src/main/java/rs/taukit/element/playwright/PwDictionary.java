package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.driver.playwright.PlaywrightDriver;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.page.PageObject;
import rs.taukit.step.allure.AllureBasicTextFieldStep;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;
import java.util.Map;

import static rs.taukit.step.utils.BasicUtils.*;

@Getter
public class PwDictionary extends PwElement implements IHasEditableValue {
    protected final PlaywrightDriver driver;
    protected Locator element;
    private String openDictionaryButtonName;
    private String dictionaryPopupName;
    private String searchFieldName;
    private String searchButtonName;
    public PwDictionary(String elementName, Locator locator) {
        super(elementName, "");
        this.driver = super.driver.castTo();
        this.element = locator;
    }
    public PwDictionary(String elementName, String locator) {
        this(elementName, new LinkedHashMap<>(Map.of("Локатор", locator)));
    }
    public PwDictionary(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
        this.driver = super.driver.castTo();
        this.element = driver.getLocator(locator);
        ElementModel model = new ElementModel(elementData);
        this.openDictionaryButtonName = model.get("кнопка");
        this.dictionaryPopupName = model.get("всплывающее окно");
        this.searchFieldName = model.get("поле поиска");
        this.searchButtonName = model.get("кнопка поиска");
    }
    public PwDictionary(String elementName, ElementModel model) {
        super(elementName, model);
        this.driver = super.driver.castTo();
        this.element = driver.getLocator(locator);
        this.openDictionaryButtonName = model.get("кнопка");
        this.dictionaryPopupName = model.get("всплывающее окно");
        this.searchFieldName = model.get("поле поиска");
        this.searchButtonName = model.get("кнопка поиска");
    }

    public PwDictionary(String elementName, IElement element) {
        this(elementName, element.getModel());
    }
    public void setValue(String value) {
        PageObject initialPage = page();
        page().getElement(openDictionaryButtonName).click();
        TauLogger.report("Нажата кнопка '%s'", openDictionaryButtonName);
        PageObject popup = pageStorage().getPopup(dictionaryPopupName);
        popup.waitForLoad();
        TauLogger.report("Отображается всплывающее окно '%s'", dictionaryPopupName);
        new AllureBasicTextFieldStep(searchFieldName).setValue(value);
        popup.getElement(searchButtonName).click();
        TauLogger.report("Нажата кнопка '%s'", searchButtonName);
        popup.getElement("Значения справочника").castTo(PwList.class).setValue(value);
        initialPage.waitForLoad();
        TauLogger.report("Отображается страница '%s'", testContext().getCurrentPageName());
    }

    @Override
    public void setValue(String value, String controlValue) {

    }

    @Override
    public boolean isVisible() {
        return element.isVisible();
    }

    @Override
    public void click() {
        element.click();
    }

    @Override
    public String getText() {
        return element.innerText();
    }
}
