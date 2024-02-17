package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasBlockElements;
import rs.taukit.element.interfaces.IHasEditableValue;
import rs.taukit.element.interfaces.IHasListItems;
import rs.taukit.exceptions.ElementListException;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.TauLogger;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class AllureBasicListStep extends BasicUtils {
    private final String listName;
    private final IHasListItems list;

    public AllureBasicListStep(String listName) {
        this.listName = listName;
        this.list = page().getElement(listName).castTo(IHasListItems.class);
    }

    @Step("в списке '{this.fieldName}' выбрать значение '{value}'")
    public void selectValue(String value) {
        try {
            ((IHasEditableValue) list).setValue(value);
            TauLogger.report("В списке \"%s\" выбрано значение \"%s\"", listName, value);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось выбрать значение \"%s\" в списке \"%s\"", value, listName);
        }
    }

    @Step("в списке \"{this.listName}\" найти {itemType}, соответствующий условиям:")
    public void findItemByConditions(String itemType, DataTable dataTable) {
        TauLogger.attachDataTable("Условия поиска элемента", dataTable);
        AtomicReference<IHasBlockElements> foundItem = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            foundItem.set(list.findItemByConditions(dataTable));
            return foundItem.get() != null;
        };
        try {
            driver().waitForCondition(condition);
        } catch (Exception e) {
            throw ElementListException.itemNotFound(listName, itemType, e).get();
        }
        int foundItemNumber = foundItem.get().getCurrentIndex() + 1;
        TauLogger.report("Найден %s № %d", itemType, foundItemNumber);
        page().setCurrentContext(foundItem.get());
        TauLogger.report("[примечание] текущий контекст: %d-й элемент списка '%s'", foundItemNumber, listName);
    }
}
