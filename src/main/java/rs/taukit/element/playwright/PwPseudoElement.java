package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import lombok.Getter;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

import static rs.taukit.utils.TextUtils.hexToRGB;

@Getter
public class PwPseudoElement extends PwElement {
    private final String pseudoClass;

    public PwPseudoElement(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwPseudoElement(String elementName, ElementModel model) {
        super(elementName, model);
        this.pseudoClass = model.get("псевдокласс");
    }


    public PwPseudoElement(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwPseudoElement(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
        this.pseudoClass = model.get("псевдокласс");
    }

    @Override
    public boolean isVisible() {
        return !isInvisible();
    }

    @Override
    public boolean isInvisible() {
        String jsExpression = String.format("""
                        (element) => window.getComputedStyle(element, '%s').getPropertyValue('display')""",
                pseudoClass);
        return elementPwLocator.count() == 0
                || elementPwLocator.evaluate(jsExpression).toString().equalsIgnoreCase("hidden");
    }

    @Override
    public void click() {
        String jsExpression = "(element) => element.click()";
        elementPwLocator.evaluate(jsExpression);
    }

    @Override
    public String getColor() {
        String jsExpression = String.format("""
                        (element) => window.getComputedStyle(element, '%s').getPropertyValue('color')""",
                pseudoClass);
        return elementPwLocator.evaluate(jsExpression).toString();
    }

    @Override
    public boolean hasColor(String colorRGB) {
        return getColor().equalsIgnoreCase(hexToRGB(colorRGB));
    }

    @Override
    public String getBgColor() {
        String jsExpression = String.format("""
                        (element) => window.getComputedStyle(element, '%s').getPropertyValue('background-color')""",
                pseudoClass);
        return elementPwLocator.evaluate(jsExpression).toString();
    }

    @Override
    public boolean hasBgColor(String colorRGB) {
        return getBgColor().equalsIgnoreCase(hexToRGB(colorRGB));
    }


    @Override
    public void hover() {
        TauLogger.console("Метод наведения курсора мыши на элемент с типом '%s' не реализован", model.getType());
    }
}
