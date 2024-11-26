package rs.taukit.testobject;

import rs.taukit.element.interfaces.*;
import rs.taukit.exceptions.AutoTestBrokenException;

import java.util.Arrays;
import java.util.function.Function;

public enum TestObjectState {
    IS_VISIBLE("отображается",
            t -> canBeVisible(t).isVisible(),
            t -> canBeVisible(t).isVisible() ? "отображается" : "не отображается"),
    IS_INVISIBLE("не отображается",
            t -> canBeVisible(t).isInvisible(),
            t -> canBeVisible(t).isVisible() ? "отображается" : "не отображается"),
    IS_ENABLED("активен",
            t -> canBeEnabled(t).isEnabled(),
            t -> canBeEnabled(t).isEnabled() ? "активен" : "неактивен"),
    IS_DISABLED("неактивен",
            t -> canBeEnabled(t).isDisabled(),
            t -> canBeEnabled(t).isEnabled() ? "активен" : "неактивен"),
    IS_CHECKED("отмечен",
            t -> canBeChecked(t).isChecked(),
            t -> canBeChecked(t).isChecked() ? "отмечен" : "не отмечен"),
    IS_NOT_CHECKED("не отмечен",
            t -> !canBeChecked(t).isChecked(),
            t -> canBeChecked(t).isChecked() ? "отмечен" : "не отмечен"),
    HAS_COLOR("имеет цвет",
            t -> hasColor(t).hasColor(t.expectedValue()),
            t -> canBeVisible(t).isVisible() ? hasColor(t).getColor() : "не отображается"),
    EMPTY("пуст",
            t -> hasText(t).getText().isEmpty(),
            t -> canBeVisible(t).isVisible() ? hasText(t).getText() : "не отображается"),
    NOT_EMPTY("не пуст",
            t -> !hasText(t).getText().isEmpty(),
            t -> canBeVisible(t).isVisible() ? hasText(t).getText() : "не отображается"),
    EQUALS("равен",
            t -> canBeVisible(t).isVisible() && hasText(t).hasText(t.expectedValue()),
            t -> canBeVisible(t).isVisible() ? hasText(t).getText() : "не отображается"),
    NOT_EQUALS("не равен",
            t -> canBeVisible(t).isVisible() && !hasText(t).hasText(t.expectedValue()),
            t -> canBeVisible(t).isVisible() ? hasText(t).getText() : "не отображается"),
    CONTAINS_TEXT("содержит",
            t -> canBeVisible(t).isVisible() && hasText(t).containsText(t.expectedValue()),
            t -> canBeVisible(t).isVisible() ? hasText(t).getText() : "не отображается"),
    NOT_CONTAINS_TEXT("не содержит",
            t -> !hasText(t).containsText(t.expectedValue()),
            t -> hasText(t).getText()),
    OPENED("открыта",
            t -> canBeOpened(t).isOpened(t.expectedValue()),
            t -> canBeOpened(t).isOpened(t.expectedValue()) ? "открыта" : "не открыта"),
    DOWNLOADED("загружен",
            t -> downloadable(t).isDownloaded(t.expectedValue()),
            t -> downloadable(t).isDownloaded(t.expectedValue()) ? "загружен" : "не загружен"),
    ;

    public final Function<TestObject, Boolean> check;
    public final Function<TestObject, ?> value;
    private final String title;

    TestObjectState(String title, Function<TestObject, Boolean> check, Function<TestObject, ?> value) {
        this.title = title;
        this.check = check;
        this.value = value;
    }

    private static CanBeVisible canBeVisible(TestObject t) {
        return (CanBeVisible) t.object();
    }

    private static CanBeEnabled canBeEnabled(TestObject t) {
        return (CanBeEnabled) t.object();
    }

    private static CanBeChecked canBeChecked(TestObject t) {
        return (CanBeChecked) t.object();
    }

    private static HasColor hasColor(TestObject t) {
        return (HasColor) t.object();
    }

    private static HasText hasText(TestObject t) {
        return (HasText) t.object();
    }

    private static CanBeOpened canBeOpened(TestObject t) {
        return (CanBeOpened) t.object();
    }

    private static Downloadable downloadable(TestObject t) {
        return (Downloadable) t.object();
    }

    public static TestObjectState get(String title) {
        return Arrays.stream(values()).filter(state -> state.title.equalsIgnoreCase(title)).findFirst()
                .orElseThrow(() -> new AutoTestBrokenException("Отсутствует описание состояния '%s'", title));
    }
}
