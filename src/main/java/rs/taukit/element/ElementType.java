package rs.taukit.element;

import lombok.Getter;
import rs.taukit.element.playwright.ElementList;
import rs.taukit.element.playwright.FileAttachment;
import rs.taukit.element.playwright.SimpleElement;
import rs.taukit.element.playwright.TextField;
import rs.taukit.exceptions.ElementTypeNotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
@Getter
public enum ElementType {
    SIMPLE_ELEMENT("элемент", SimpleElement.class),
    TEXT_FIELD("поле", TextField.class),
    ELEMENT_LIST("список", ElementList.class),
    FILE("файл", FileAttachment.class);

    private static final Map<String, ElementType> BY_TYPE = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(value -> BY_TYPE.put(value.typeName, value));
    }

    private final String typeName;
    private final Class<IElement> elementClass;

    ElementType(String typeName, Class<? extends IElement> elementClass) {
        this.typeName = typeName;
        this.elementClass = (Class<IElement>) elementClass;
    }

    public static ElementType getType(String typeName) {
        return Optional.ofNullable(BY_TYPE.get(typeName)).orElseThrow(() -> new ElementTypeNotFoundException(typeName));
    }
}
