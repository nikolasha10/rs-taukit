package rs.taukit.element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum ElementState {
    IS_VISIBLE("отображается", IElement::isVisible, "не отображается"),
    IS_NOT_VISIBLE("не отображается", element -> !element.isVisible(), "отображается");

    private static final Map<String, ElementState> BY_NAME = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(value -> BY_NAME.put(value.stateName, value));
    }

    private final String stateName;
    private final Function<IElement, Boolean> checkState;
    private final String wrongState;

    ElementState(String stateName, Function<IElement, Boolean> checkState, String wrongState) {
        this.stateName = stateName;
        this.checkState = checkState;
        this.wrongState = wrongState;
    }

    public static Function<IElement, Boolean> getState(String stateName) {
        return BY_NAME.get(stateName).checkState;
    }

    public static String getWrongState(String stateName) {
        return BY_NAME.get(stateName).wrongState;
    }

}
