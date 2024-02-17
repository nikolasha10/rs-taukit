package rs.taukit.page.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.playwright.PwElement;
import rs.taukit.exceptions.ElementNotBuiltException;
import rs.taukit.utils.YamlParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ElementBuilder {
    private static final Map<String, ElementModel> modelMap = new HashMap<>();

    static {
        Map<String, Map<String, Object>> elementModels = YamlParser.parse(Config.getProperty("element.model.file"));
        elementModels.forEach((modelName, structure) -> modelMap.put(modelName, new ElementModel(structure)));
    }

    public static void buildElements(Map<String, Map<String, Object>> elements, Map<String, IElement> result) {
        if (elements == null) return;
        List<String> brockenElementList = elements.keySet().stream().filter(elementName -> !(elements.get(elementName) instanceof Map)).toList();
        if (!brockenElementList.isEmpty()) {
            throw ElementNotBuiltException.modelIncorrect(brockenElementList).get();
        }
        elements.forEach((elementName, elementStructure) -> result.put(
                elementName, buildElement(elementName, new ElementModel(elementStructure))));
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

    private static IElement buildElement(String elementName, ElementModel model) {
        String elementTypeName = model.get("тип").toString();
        ElementModel protoModel = Optional.ofNullable(modelMap.get(elementTypeName))
                .orElseThrow(ElementNotBuiltException.modelPrototypeNotFound(elementName, elementTypeName));
        ElementModel elementModel = protoModel.replace(model);
        String className = protoModel.get("класс");
        try {
            Class<? extends IElement> elementClass = getClassByName(elementName, elementTypeName);
            return buildElement(elementName, elementClass, elementModel);
        } catch (Exception e) {
            throw new ElementNotBuiltException(e, elementName, className, elementModel.toString());
        }
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

    @SuppressWarnings("unchecked")
    public static Class<? extends IElement> getClassByName(String elementName, String elementTypeName) {
        ElementModel protoModel = Optional.ofNullable(modelMap.get(elementTypeName))
                .orElseThrow(ElementNotBuiltException.modelPrototypeNotFound(elementName, elementTypeName));
        String className = protoModel.get("класс");
        try {
            return (Class<? extends IElement>) IElement.class.getClassLoader().loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
