package rs.taukit.element;

import rs.taukit.element.interfaces.IHasText;
import rs.taukit.element.playwright.PwBrowserTab;
import rs.taukit.exceptions.ElementStateNotFoundException;
import rs.taukit.records.CheckedElement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static rs.taukit.utils.BasicUtils.page;

public enum ElementState {
    IS_VISIBLE("отображается", checked -> checked.element().isVisible(), "не отображается"),
    IS_NOT_VISIBLE("не отображается", checked -> !checked.element().isVisible(), "отображается"),
    IS_DOWNLOADED("загружен", checked -> !checked.element().isVisible(), "не загружен"),
    IS_OPENED("открыта", checked -> checked.element().castTo(PwBrowserTab.class).isOpened(), "не открыта"),
    CONTENT_TEXT("содержит", checked -> page()
            .replaceElement(checked.element().getElementName(), checked.elementType())
            .castTo(IHasText.class)
            .getText()
            .equals(checked.expectedValue()), "не содержит ожидаемый текст");

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

    public static Function<CheckedElement, Boolean> getState(String stateName) {
        return BY_NAME.get(stateName).checkState;
    }

    public static String getWrongState(String stateName) {
        return Optional.ofNullable(BY_NAME.get(stateName))
                .orElseThrow(ElementStateNotFoundException.get(stateName)).wrongState;
    }

}
