package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicBlockElementStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicBlockElementStep {
    private AllureBasicBlockElementStep blockElement(String blockElementName, String fieldName) {
        return new AllureBasicBlockElementStep(blockElementName, fieldName);
    }

    @И("^в блоке \"(.+)\" нажать на поле \"(.+)\"$")
    public void clickOnField(String blockName, String fieldName) {
        blockElement(blockName, fieldName).clickOnField();
    }

    @И("^в блоке \"(.+)\" ввести в поле \"(.+)\" значение \"(.+)\"$")
    public void fill(String blockName, String fieldName, String value) {
        blockElement(blockName, fieldName).setValue(eval(value));
    }

    @И("^в блоке \"(.+)\" ввести в поле \"(.+)\" значение \"(.+)\" и проконтролировать$")
    public void fillWithControl(String blockName, String fieldName, String value) {
        blockElement(blockName, fieldName).setValue(eval(value), eval(value));
    }

    @И("^сохранить значения поля \"(.+)\" в блоке \"(.+)\", как \"(.+)\"$")
    public void saveElementValue(String blockName, String fieldName, String parameterName) {
        blockElement(blockName, fieldName).saveElementValue(parameterName);
    }

    @И("^в блоке \"(.+)\" установить чекбокс \"(.+)\"$")
    public void setCheckbox(String blockName, String fieldName) {
        blockElement(blockName, fieldName).setCheckbox();
    }

}
