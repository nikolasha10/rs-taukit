package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.ListManager;

import static rs.taukit.utils.BasicUtils.eval;

/**
 * Шаги для взаимодействия со списковыми элементами
 */
public class ListStep extends ListManager {

    @И("^в списке \"(.+)\" выбрать \"(.+)\"$")
    public void selectItem(String listName, String itemName) {
        super.selectItem(listName, eval(itemName));
    }

    @И("^в списке \"(.+)\" найти (элемент|блок), соответствующий условиям:$")
    public void selectValue(String listName, String elementType, DataTable dataTable) {
        super.findItemByConditions(eval(listName), elementType, eval(dataTable));
    }

    /**
     * @param listName    название списка
     * @param number      порядковый номер элемента
     * @param elementType тип элемента: <br>
     *                    элемент - будет произведён клик <br>
     *                    блок - будет установлен в качестве текущего контекста
     */
    @И("^в списке \"(.+)\" выбрать (\\d+)-й (элемент|блок)$")
    public void selectValue(String listName, String number, String elementType) {
        super.selectItem(listName, eval(number), elementType);
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

    @И("^проверить, что все блочные элементы списка \"(.+)\" соответствуют условиям:$")
    public void validateAllBlockElements(String listName, DataTable dataTable) {
        super.validateAllBlockElements(listName, eval(dataTable));
    }

    @И("^проверить, что список \"(.+)\" содержит ровно \"(.+)\" (элемент(?:а|ов)?)$")
    public void validateListElementsCountEquals(String listName, String count, String word) {
        super.validateListElementsCountEquals(listName, eval(count), word);
    }

}
