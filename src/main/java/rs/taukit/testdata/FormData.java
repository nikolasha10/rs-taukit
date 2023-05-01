package rs.taukit.testdata;

import io.cucumber.datatable.DataTable;
import rs.taukit.driver.DriverManager;
import rs.taukit.driver.WebDriver;
import rs.taukit.element.ElementType;
import rs.taukit.element.IElement;
import rs.taukit.element.IHasEditableValue;
import rs.taukit.exceptions.DataTableFormatMismatchException;
import rs.taukit.page.PageObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FormData {
    private final PageObject pageObject;
    private final List<Map<String, String>> data;
    private final WebDriver driver;

    public FormData(DataTable dataTable, PageObject pageObject) {
        this.data = dataTable.asMaps();
        this.pageObject = pageObject;
        this.driver = DriverManager.getInstance().getDriver();
    }

    public void fillForm() {
        data.forEach((row) -> {
            String type = Optional.ofNullable(row.get("Тип"))
                    .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле 'Тип'"));
            String name = Optional.ofNullable(row.get("Название"))
                    .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле 'Название'"));
            String value = Optional.ofNullable(row.get("Значение"))
                    .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле 'Значение'"));
            String control = row.get("Контроль");
            Class<IElement> elementType = ElementType.getType(type).getElementClass();
            IHasEditableValue element = pageObject.replaceElement(name, elementType).castTo(IHasEditableValue.class);
            if (control == null) {
                element.setValue(value);
            } else {
                element.setValue(value, control);
            }
        });
    }
}
