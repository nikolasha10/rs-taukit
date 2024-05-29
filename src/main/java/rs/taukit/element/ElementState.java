package rs.taukit.element;

import rs.taukit.element.playwright.PwBrowserTab;
import rs.taukit.exceptions.ElementStateNotFoundException;
import rs.taukit.records.CheckedElement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Перечисление, описывающее возможные состояния проверяемых объектов и методы проверки этих состояний
 */
public enum ElementState {
    IS_VISIBLE("отображается", checked -> checked.element().isVisible(), "не отображается"),
    IS_NOT_VISIBLE("не отображается", checked -> !checked.element().isVisible(), "отображается"),
    IS_SHE_ENABLED("активна", checked -> checked.element().isEnabled(), "не активна"),
    IS_HE_ENABLED("активен", checked -> checked.element().isEnabled(), "не активен"),
    IS_IT_ENABLED("активно", checked -> checked.element().isEnabled(), "не активно"),
    HAS_COLOR("имеет цвет", checked -> checked.element().hasBgColor(checked.expectedValue().toString()), "имеет другой цвет"),
    IS_DOWNLOADED("загружен", checked -> !checked.element().isVisible(), "не загружен"),
    IS_OPENED("открыта", checked -> checked.element().castTo(PwBrowserTab.class).isOpened(), "не открыта"),
    CONTENT_TEXT("содержит", checked -> {
        String elementText = checked.element().getText();
        return elementText.contains(checked.expectedValue().toString());
    }, "не содержит ожидаемый текст"),
    NOT_CONTENT_TEXT("не содержит", checked -> {
        String elementText = checked.element().getText();
        return !elementText.contains(checked.expectedValue().toString());
    }, "содержит текст, отсутствие которого ожидалось"),
    TEXT_EQUALS("равен", checked -> {
        String elementText = checked.element().getText();
        return elementText.equals(checked.expectedValue().toString());
    }, "отличается от ожидаемого текста"),
    TEXT_NOT_EQUALS("не равен", checked -> {
        String elementText = checked.element().getText();
        return !elementText.equals(checked.expectedValue().toString());
    }, "совпадает с текстом, отсутствие которого ожидалось");

    private static final Map<String, ElementState> BY_NAME = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(value -> BY_NAME.put(value.stateName, value));
    }

    private final String stateName;
    private final Function<CheckedElement, Boolean> checkState;
    private final String wrongState;

    ElementState(String stateName, Function<CheckedElement, Boolean> checkState, String wrongState) {
        this.stateName = stateName;
        this.checkState = checkState;
        this.wrongState = wrongState;
    }

    /**
     * Возвращает функцию, принимающую на вход объект Record с параметрами проверяемого элемент, и возвращающую логическое значение
     *
     * @param stateName - название состояния элемента, определяющее метод его проверки
     * @return true, если состояние соответствует ожидаемому, иначе false
     */
    public static Function<CheckedElement, Boolean> getState(String stateName) {
        return BY_NAME.get(stateName).checkState;
    }

    public static String getWrongState(String stateName) {
        return Optional.ofNullable(BY_NAME.get(stateName))
                .orElseThrow(ElementStateNotFoundException.get(stateName)).wrongState;
    }

}
