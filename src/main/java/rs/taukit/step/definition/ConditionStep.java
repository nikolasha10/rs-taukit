package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.ConditionManager;

import static rs.taukit.utils.BasicUtils.eval;

public class ConditionStep extends ConditionManager {
    @И("^проверить выполнение условий \"(.+)\":$")
    public void checkConditions(String conditionsName, DataTable datatable) {
        super.checkConditions(conditionsName, eval(datatable));
    }

    @И("^дождаться выполнения условий:$")
    public void waitForConditions(DataTable datatable) {
        super.waitForConditionsNoName(eval(datatable));
    }

    @И("^дождаться выполнения условий в течение \"(.+)\":$")
    public void expectedResultCheckWithinTimeInterval(String timeoutString, DataTable datatable) {
        super.waitForConditionsWithinTimeIntervalNoName(timeoutString, eval(datatable));
    }

    @И("^дождаться выполнения условий \"(.+)\" в течение \"(.+)\":$")
    public void expectedResultCheckWithinTimeInterval(String conditionsName, String timeoutString, DataTable datatable) {
        super.waitForConditionsWithinTimeIntervalNoName(eval(conditionsName), timeoutString, eval(datatable));
    }

    /**
     * Ожидает выполнение условий, перечисленных в таблице datatable
     *
     * @param conditionsName название контейнера условий, на которое ссылается служебный шаг повторения действий
     * @param datatable      таблица условий. Формат:<br>
     *                       <nobr>| Объект | Название | Состояние | Значение | Ожидание |</nobr>
     *                       <br><br>Объект:
     *                       <ul>
     *                           <li>вкладка</li>
     *                           <li>страница</li>
     *                           <li>[любой тип элемента]</li>
     *                       </ul><br>Состояние:
     *                       <ul>
     *                          <li>отображается</li>
     *                          <li>активен</li>
     *                          <li>и т.д., одно из значений поля title перечисления TestObjectState.java</li>
     *                       </ul><br>Ожидание: задаёт время ожидания выполнения условия в данной строке.
     *                       Например, 1 минута
     */
    @И("^дождаться выполнения условий \"([а-яА-Я\\w\\d\\s'\\,:/\\-]+)\":$")
    public void waitForConditions(String conditionsName, DataTable datatable) {
        super.waitForConditions(conditionsName, eval(datatable));
    }

}
