package rs.taukit.page;

import rs.taukit.exceptions.ElementStateNotFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum PageState {
    IS_DISPLAYED("отображается", PageObject::isPageLoaded, "не отображается"),
    IS_NOT_DISPLAYED("не отображается", PageObject::isPageClosed, "отображается");

    private static final Map<String, PageState> BY_NAME = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(value -> BY_NAME.put(value.stateName, value));
    }

    private final String stateName;
    private final Function<PageObject, Boolean> checkState;
    private final String wrongState;

    PageState(String stateName, Function<PageObject, Boolean> checkState, String wrongState) {
        this.stateName = stateName;
        this.checkState = checkState;
        this.wrongState = wrongState;
    }

    public static Function<PageObject, Boolean> getState(String stateName) {
        return BY_NAME.get(stateName).checkState;
    }

    public static String getWrongState(String stateName) {
        return Optional.ofNullable(BY_NAME.get(stateName))
                .orElseThrow(ElementStateNotFoundException.get(stateName)).wrongState;
    }
}
