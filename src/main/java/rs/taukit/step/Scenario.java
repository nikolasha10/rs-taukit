package rs.taukit.step;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.qameta.allure.Step;
import rs.taukit.utils.TauLogger;

import static rs.taukit.step.Utils.eval;

public class Scenario {
    /**
     * Читает файл <имя сценария>
     */
    @Когда("^подготовить тестовые параметры$")
    public void testParameterInitFromFile() {
        Story.testParameterInit();
    }

    @И("^открыть браузер \"(chromium|webkit|firefox)\"$")
    public void openBrowser(String browserName) {
        Story.openBrowser(browserName);
    }
    @И("^открыть страницу \"(.+)\"$")
    public void openPage(String pageName) {
        Story.openPage(pageName);
    }

    @И("^нажать на элемент \"(.+)\"$")
    public static void click(String elementName) {
        Story.click(elementName);
    }

    @И("^нажать на кнопку \"(.+)\"$")
    public static void clickButton(String buttonName) {
        Story.clickButton(buttonName);
    }

    @И("^заполнить поля формы значениями:$")
    public void fillFormFields(DataTable dataTable){
        Story.fillFormFields(eval(dataTable));
    }

    @И("^сделать скриншот$")
    public void takeScreenshot(){
        Story.takeScreenshot();
    }

}
