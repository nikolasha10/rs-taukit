package rs.taukit.testobject;

import rs.taukit.element.interfaces.*;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.testdata.TestData;

import java.util.Arrays;
import java.util.function.Function;

import static rs.taukit.utils.BasicUtils.*;

public enum TestObjectType {
    PAGE("страница", name -> browser().getPage(name)),
    POPUP("всплывающее окно", name -> page().getPopup(name)),
    ELEMENT("элемент", name -> element(name).castTo(IElement.class)),
    INPUT("поле", name -> element(name).castTo(Editable.class)),
    TEXT("текст", name -> element(name).castTo(HasText.class)),
    BUTTON("кнопка", name -> element(name).castTo(IElement.class)),
    RADIO_BUTTON("радио-кнопка", name -> element(name).castTo(IElement.class)),
    LINK("ссылка", name -> element(name).castTo(IElement.class)),
    DATE("дата", name -> element(name).castTo(IElement.class)),
    CHECKBOX("чекбокс", name -> element(name).castTo(CanBeChecked.class)),
    TOGGLE("переключатель", name -> element(name).castTo(Editable.class)),
    DROPDOWN_LIST("выпадающий список", name -> element(name).castTo(IElement.class)),
    LIST("список", name -> element(name).castTo(HasListItems.class)),
    CONTAINER("контейнер", name -> element(name).castTo(IContainer.class)),
    TABLE("таблица", name -> element(name).castTo(IHasTableElements.class)),
    TABLE_HEADER("столбец", name -> {
        IHasTableElements table = scenarioData().get("контекст таблицы");
        return table.getActiveCell(name);
    }),
    SEARCH_FIELD("поиск по значению", name -> element(name).castTo(Editable.class)),
    PARAMETER("параметр", name -> {
        Object optional = scenarioData().getOptional(name);
        String value = optional == null ? null : String.valueOf(optional);
        return new TestData(value);
    });

    public final Function<String, ?> testObject;
    private final String title;

    TestObjectType(String title, Function<String, ?> testObject) {
        this.title = title;
        this.testObject = testObject;
    }

    public static TestObjectType get(String title) {
        return Arrays.stream(values()).filter(type -> type.title.equalsIgnoreCase(title)).findFirst()
                .orElseThrow(() -> new AutoTestBrokenException("Отсутствует описание типа тестового объекта: %s", title));
    }
}
