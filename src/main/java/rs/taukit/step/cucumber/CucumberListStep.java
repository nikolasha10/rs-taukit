package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicListStep;

import static rs.taukit.utils.BasicUtils.eval;

/**
 * Шаги для взаимодействия со списковыми элементами
 */
public class CucumberListStep {
    private AllureBasicListStep list(String listName) {
        return new AllureBasicListStep(listName);
    }

    @И("^в списке \"(.+)\" выбрать \"(.+)\"$")
    public void listValueClick(String listName, String listValue) {
        list(listName).listValueClick(eval(listValue));
    }

    @И("^в списке \"(.+)\" найти (элемент|блок), соответствующий условиям:$")
    public void selectValue(String listName, String elementType, DataTable dataTable) {
        list(listName).findItemByConditions(elementType, eval(dataTable));
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
        list(listName).validateElements(eval(dataTable));
    }

}
