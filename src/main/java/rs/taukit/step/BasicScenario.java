package rs.taukit.step;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;

import static rs.taukit.step.utils.BasicUtils.eval;

public class BasicScenario {
    /**
     * Считывает тестовые данные для текущего сценария из yaml-файла с таким же названием,
     * как feature-файл, содержащий этот сценарий. Блок данных ищется по названию сценария.
     */
    @Когда("^подготовить тестовые параметры$")
    public void testParameterInitFromFile() {
        BasicStory.testParameterInit();
    }

    @И("^открыть браузер$")
    public void openBrowser() {
        BasicStory.openBrowser();
    }
    @И("^открыть страницу \"(.+)\"$")
    public void openPage(String pageName) {
        BasicStory.openPage(pageName);
    }
    @И("^дождаться отображения страницы \"(.+)\"$")
    public static void waitForPageLoad(String pageName) {
        BasicStory.waitForPageLoad(pageName);
    }
    @И("^дождаться отображения всплывающего окна \"(.+)\"$")
    public static void waitForPopupWindow(String pageName) {
        BasicStory.waitForPopupWindow(pageName);
    }
    @И("^ввести в поле \"(.+)\" значение \"(.+)\"$")
    public static void fill(String fieldName, String value) {
        BasicStory.setValue(fieldName,eval(value));
    }
    @И("^нажать на элемент \"(.+)\"$")
    public static void click(String elementName) {
        BasicStory.clickElement(elementName);
    }

    @И("^нажать на кнопку \"(.+)\"$")
    public static void clickButton(String buttonName) {
        BasicStory.clickButton(buttonName);
    }

    @И("^заполнить поля формы значениями:$")
    public void fillFormFields(DataTable dataTable){
        BasicStory.fillFormFields(eval(dataTable));
    }
    @И("^в списке \"(.+)\" выбрать значение \"(.+)\"$")
    public void selectListValue(String listName, String value){
        BasicStory.selectListValue(listName, eval(value));
    }

    @И("^сделать скриншот$")
    public void takeScreenshot(){
        BasicStory.takeScreenshot();
    }

    @И("^подождать (\\d+) (секунд|минут)$")
    public void waitForTimeout(String timeValue, String timeUnit){
        BasicStory.waitForTimeout(timeValue, timeUnit);
    }

    @Тогда("^ожидаемый результат:$")
    public void expectedResultCheck(DataTable datatable){
        BasicStory.expectedResultCheck(eval(datatable));
    }
}
