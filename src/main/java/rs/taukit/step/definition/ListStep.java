package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.ListManager;

import static rs.taukit.utils.BasicUtils.eval;

/**
 * Шаги для взаимодействия со списковыми элементами
 */
public class ListStep extends ListManager{

    @И("^в списке \"(.+)\" выбрать \"(.+)\"$")
    public void selectItem(String listName, String itemName) {
        super.selectItem(listName, eval(itemName));
    }

    @И("^в списке \"(.+)\" найти (элемент|блок), соответствующий условиям:$")
    public void selectValue(String listName, String elementType, DataTable dataTable) {
        super.findItemByConditions(listName, elementType, eval(dataTable));
    }

    /**
     * Проверяет наличие/отсутствие элементов в списке
     *
     * @param listName  - название списка
     * @param dataTable - таблица данных, вида <br>
     *                  <table><tr><th>Название</th><th>Состояние</th></tr>
     *                  <tr><td><i>Текст элемента списка</i></td><td>отображается / не отображается </td></tr>
     *                  </table>
     */
    @И("^проверить состав элементов списка \"(.+)\":$")
    public void validateElements(String listName, DataTable dataTable) {
        super.validateElements(listName, eval(dataTable));
    }

}
