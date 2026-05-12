package rs.taukit.testobject;

import rs.taukit.element.interfaces.*;
import rs.taukit.exceptions.AutoTestBrokenException;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static rs.taukit.utils.BasicUtils.scenarioData;

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
            t -> hasColor(t).getColor()),
    HAS_BG_COLOR("имеет цвет фона",
            t -> hasColor(t).hasBgColor(t.expectedValue()),
            t -> hasColor(t).getBgColor()),
    EMPTY("пуст",
            t -> hasText(t).getText().isEmpty(),
            t -> hasText(t).getText()),
    NOT_EMPTY("не пуст",
            t -> !hasText(t).getText().isEmpty(),
            t -> hasText(t).getText()),
    EQUALS("равен",
            t -> hasText(t).hasText(t.expectedValue()),
            t -> hasText(t).getText()),
    EQUALS_IGNORE_CASE("равен без учёта регистра",
            t -> hasText(t).getText().equalsIgnoreCase(t.expectedValue()),
            t -> hasText(t).getText()),
    NOT_EQUALS_IGNORE_CASE("не равен без учёта регистра",
            t -> !hasText(t).getText().equalsIgnoreCase(t.expectedValue()),
            t -> hasText(t).getText()),
    NOT_EQUALS("не равен",
            t -> !hasText(t).hasText(t.expectedValue()),
            t -> hasText(t).getText()),
    CONTAINS_TEXT("содержит",
            t -> hasText(t).containsText(t.expectedValue()),
            t -> hasText(t).getText()),
    NOT_CONTAINS_TEXT("не содержит",
            t -> !hasText(t).containsText(t.expectedValue()),
            t -> hasText(t).getText()),
    CONTAINS_TEXT_IGNORE_CASE("содержит без учёта регистра",
            t -> hasText(t).getText().toLowerCase().contains(t.expectedValue().toLowerCase()),
            t -> hasText(t).getText()),
    NOT_CONTAINS_TEXT_IGNORE_CASE("не содержит без учёта регистра",
            t -> !hasText(t).getText().toLowerCase().contains(t.expectedValue().toLowerCase()),
            t -> hasText(t).getText()),
    MATCHES("соответствует формату",
            t -> hasText(t).matches(t.expectedValue()),
            t -> hasText(t).getText()),
    NOT_MATCHES("не соответствует формату",
            t -> !hasText(t).matches(t.expectedValue()),
            t -> hasText(t).getText()),
    OPENED("открыта",
            t -> canBeOpened(t).isOpened(t.expectedValue()),
            t -> canBeOpened(t).isOpened(t.expectedValue()) ? "открыта" : "не открыта"),
    DOWNLOADED("загружен",
            t -> downloadable(t).isDownloaded(t.expectedValue()),
            t -> downloadable(t).isDownloaded(t.expectedValue()) ? "загружен" : "не загружен"),
    CONTAINS_ITEM("содержит пункт",
            t -> hasMenu(t).containsItem(t.expectedValue()),
            t -> hasMenu(t).containsItem(t.expectedValue()) ? "содержит пункт" : "не содержит пункт"),
    CONSISTS_OF("состоит из элементов",
            t -> hasListItems(t).containsItems(scenarioData().get(t.expectedValue()), true),
            t -> hasListItems(t).containsItems(scenarioData().get(t.expectedValue()), true)
                    ? "состоит из элементов" : hasListItems(t).getItemList().stream()
                    .map(item -> item.castTo(HasText.class).getText()).collect(Collectors.joining(", "))),
    IS_EMPTY("не содержит элементов",
            t -> hasListItems(t).getItemList().isEmpty(),
            t -> hasListItems(t).getItemList().isEmpty() ? "не содержит элементов" : "содержит " + hasListItems(t).getItemList().size() + " элементов"),
    INCLUDED_IN("содержится в списке",
            t -> isIncludedIn(hasText(t), scenarioData().get(t.expectedValue())),
            t -> isIncludedIn(hasText(t), scenarioData().get(t.expectedValue()))
                    ? "содержится в списке" : "не содержится в списке"),
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

    private static IHasMenu hasMenu(TestObject t) {
        return (IHasMenu) t.object();
    }

    private static HasListItems hasListItems(TestObject t) {
        return (HasListItems) t.object();
    }

    private static boolean isIncludedIn(HasText item, List<String> list) {
        return list.contains(item.getText());
    }

    public static TestObjectState get(String title) {
        return Arrays.stream(values()).filter(state -> state.title.equalsIgnoreCase(title)).findFirst()
                .orElseThrow(() -> new AutoTestBrokenException("Отсутствует описание состояния '%s'", title));
    }
}
