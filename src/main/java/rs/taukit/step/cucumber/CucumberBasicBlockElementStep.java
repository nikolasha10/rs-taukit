package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicBlockElementStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicBlockElementStep {
    private AllureBasicBlockElementStep blockElement(String blockElementName) {
        return new AllureBasicBlockElementStep(blockElementName);
    }

    @И("^в блоке \"(.+)\" нажать на (элемент|поле|кнопку) \"(.+)\"$")
    public void clickOnField(String blockName, String fieldType, String fieldName) {
        blockElement(blockName).clickOnField(fieldType, fieldName);
    }

    @И("^в блоке \"(.+)\" ввести в поле \"(.+)\" значение \"(.+)\"$")
    public void fill(String blockName, String fieldName, String value) {
        blockElement(blockName).setValue(fieldName, eval(value));
    }

    @И("^в блоке \"(.+)\" ввести в поле \"(.+)\" значение \"(.+)\" и проконтролировать$")
    public void fillWithControl(String blockName, String fieldName, String value) {
        blockElement(blockName).setValue(eval(value), eval(value));
    }

    @И("^сохранить значения поля \"(.+)\" в блоке \"(.+)\", как \"(.+)\"$")
    public void saveElementValue(String blockName, String fieldName, String parameterName) {
        blockElement(blockName).saveElementValue(fieldName, parameterName);
    }

    @И("^в блоке \"(.+)\" установить чекбокс \"(.+)\"$")
    public void setCheckbox(String blockName, String fieldName) {
        blockElement(blockName).setCheckbox(fieldName);
    }

    @И("^в блоке \"(.+)\" в списке \"(.+)\" выбрать \"(.+)\"$")
    public void listValueClick(String blockName, String listName, String itemName) {
        blockElement(blockName).listValueClick(listName, itemName);
    }

    @И("^в блоке \"(.+)\" нажать на \"(.+)\" и скачать файл \"(.+)\"$")
    public void downloadFile(String blockName, String fileName, String buttonName) {
        blockElement(blockName).downloadFile(eval(fileName), buttonName);
    }

    @И("^в блоке \"(.+)\" нажать на \"(.+)\" и выбрать загруженный файл \"(.+)\"$")
    public void attachFile(String blockName, String fileName, String buttonName) {
        blockElement(blockName).attachFile(eval(fileName), buttonName);
    }

    @И("^дождаться отображения блока \"(.+)\"$")
    public void waitForElement(String blockElementName) {
        blockElement(eval(blockElementName)).waitForDisplayed();
    }

}
