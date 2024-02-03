package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.page.PageObject;
import rs.taukit.page.playwright.ElementBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.*;

@Getter
public class PwDictionary extends PwElement implements IHasEditableValue {
    private String openDictionaryButtonSelector;
    private String dictionaryPopupName;
    private String searchFieldName;
    private String searchButtonName;

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
        this.openDictionaryButtonSelector = model.get("кнопка");
        this.dictionaryPopupName = model.get("всплывающее окно");
        this.searchFieldName = model.get("поле поиска");
        this.searchButtonName = model.get("кнопка поиска");
    }

    public PwDictionary(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        this.openDictionaryButtonSelector = model.get("кнопка");
        this.dictionaryPopupName = model.get("всплывающее окно");
        this.searchFieldName = model.get("поле поиска");
        this.searchButtonName = model.get("кнопка поиска");
    }

    public PwDictionary(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public void setValue(String value) {
        PageObject initialPage = page();
        PwElement openDictionaryButton = ElementBuilder.buildElement("кнопка", openDictionaryButtonSelector)
                .castTo(PwElement.class);
        element.locator(openDictionaryButton.getElement()).click();
        PageObject popup = pageStorage().getPopup(dictionaryPopupName);
        popup.waitForLoad();
        new PwTextField(searchFieldName, popup.getElement(searchFieldName)).setValue(value);
        popup.getElement(searchButtonName).click();
        popup.getElement("Значения справочника").castTo(PwList.class).setValue(value);
        popup.waitForClose();
        browser().getPage(initialPage.getPageName());
        initialPage.waitForLoad();
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
