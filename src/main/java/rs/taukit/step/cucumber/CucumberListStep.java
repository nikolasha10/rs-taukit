package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicListStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberListStep {
    private AllureBasicListStep list(String listName) {
        return new AllureBasicListStep(listName);
    }

    @И("^в списке \"(.+)\" найти (элемент|блок), соответствующий условиям:$")
    public void selectValue(String listName, String elementType, DataTable dataTable) {
        list(listName).findItemByConditions(elementType, eval(dataTable));
    }

}
