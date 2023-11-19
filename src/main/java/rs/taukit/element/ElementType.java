package rs.taukit.element;

import lombok.Getter;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.playwright.*;
import rs.taukit.exceptions.ElementTypeNotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
@Getter
public enum ElementType {
    SIMPLE_ELEMENT("элемент", PwElement.class),
    TEXT_FIELD("поле", PwTextField.class),
    SEARCH_FIELD("поиск по значению", PwSearchField.class),
    ELEMENT_LIST("список", PwList.class),
    ELEMENT_BLOCK("блок элементов", PwBlock.class),
    MENU("меню", PwMenu.class),
    TOGGLE("переключатель", PwToggle.class),
    CHECKBOX("чекбокс", PwCheckbox.class),
    RADIO_BUTTON("радио-кнопка", PwRadioButton.class),
    DROPDOWN_LIST("выпадающий список", PwDropdownList.class),
    LINK("ссылка", PwLink.class),
    ELEMENT_TABLE("таблица", PwTable.class),
    FILE("файл", FileAttachment.class),
    DOWNLOAD("загрузка файла", FileDownload.class),
    DICTIONARY("справочник", PwDictionary.class),
    DATE("дата", PwTextField.class);

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
