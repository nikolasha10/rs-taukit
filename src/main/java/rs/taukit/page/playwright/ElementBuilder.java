package rs.taukit.page.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.playwright.PwElement;
import rs.taukit.element.playwright.PwTextField;
import rs.taukit.exceptions.ElementNotBuiltException;

import java.util.HashMap;
import java.util.Map;

public class ElementBuilder {
    @SuppressWarnings("unchecked")
    public static Map<String, IElement> buildElements(Map<String, ?> elements) {
        Map<String, IElement> result = new HashMap<>();
        elements.forEach((elementName, elementObject) -> {
            if (elementObject instanceof String locator) {
                result.put(elementName, buildElement(elementName, locator));
            } else if (elementObject instanceof Map elementData) {
                IElement element = buildElement(elementName, elementData);
                result.put(elementName, element);
            }
        });
        return result;
    }

    public static IElement buildElement(String elementName, String locator) {
        return new PwTextField(elementName, locator);
    }

    public static IElement buildElement(String elementName, PwElement element, Locator locator) {
        Class<? extends IElement> elementClass = element.getClass();
        try {
            return elementClass.getConstructor(String.class, Locator.class, ElementModel.class)
                    .newInstance(elementName, locator, element.getModel());
        } catch (Exception e) {
            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), element.getModel().toString());
        }
    }

    private static IElement buildElement(String elementName, Map<String, Object> elementDataInitial) {
        ElementModel model = new ElementModel(elementDataInitial);
        String elementTypeName = model.get("тип").toString();
        Class<? extends IElement> elementClass = ElementType.getType(elementTypeName).getElementClass();
        return buildElement(elementName, elementClass, model);
    }

    public static IElement buildElement(String elementName, Class<? extends IElement> elementClass, ElementModel model) {
        try {
            return elementClass.getConstructor(String.class, ElementModel.class).newInstance(elementName, model);
        } catch (Exception e) {
            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), model.toString());
        }
    }

    public static IElement buildElement(String elementName, Class<? extends IElement> elementClass, IElement element) {
        try {
            return elementClass.getConstructor(String.class, IElement.class).newInstance(elementName, element);
        } catch (Exception e) {
            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), element.getModel().toString());
        }
    }
}
