package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.ContainerManager;

import static rs.taukit.utils.BasicUtils.eval;

public class ContainerStep extends ContainerManager {
    
    @И("^в контейнере \"(.+)\" нажать на (элемент|поле|кнопку) \"(.+)\"$")
    public void clickOnField(String containerName, String fieldType, String fieldName) {
        super.clickOnField(eval(containerName), fieldType, fieldName);
    }

    @И("^в контейнере \"(.+)\" ввести в поле \"(.+)\" значение \"(.+)\"$")
    public void fill(String containerName, String fieldName, String value) {
        super.setValue(eval(containerName), fieldName, eval(value));
    }

    @И("^в контейнере \"(.+)\" ввести в поле \"(.+)\" значение \"(.+)\" и проконтролировать$")
    public void fillWithControl(String containerName, String fieldName, String value) {
        super.setValue(eval(containerName), eval(value), eval(value));
    }

    @И("^сохранить значения поля \"(.+)\" в контейнере \"(.+)\", как \"(.+)\"$")
    public void saveElementValue(String containerName, String fieldName, String parameterName) {
        super.saveElementValue(eval(containerName), fieldName, parameterName);
    }

    @И("^в контейнере \"(.+)\" установить чекбокс \"(.+)\"$")
    public void setCheckbox(String containerName, String fieldName) {
        super.setCheckbox(eval(containerName), fieldName);
    }
    @И("^в контейнере \"(.+)\" установить переключатель \"(.+)\" в значение \"(.+)\"$")
    public void setRadioButton(String containerName, String fieldName, String value) {
        super.setRadioButton(eval(containerName), fieldName, eval(value));
    }

    @И("^в контейнере \"(.+)\" в списке \"(.+)\" выбрать \"(.+)\"$")
    public void listValueClick(String containerName, String listName, String itemName) {
        super.listValueClick(eval(containerName), listName, eval(itemName));
    }

    @И("^в контейнере \"(.+)\" в списке \"(.+)\" найти элемент с текстом \"(.+)\"$")
    public void listItemFindByValue(String containerName, String listName, String itemValue) {
        super.listItemFindByValue(eval(containerName), listName, eval(itemValue));
    }

    @И("^в контейнере \"(.+)\" в списке \"(.+)\" выбрать элемент № (.+)$")
    public void listItemClickByNumber(String containerName, String listName, String itemNumber) {
        super.listItemClickByNumber(eval(containerName), listName, eval(itemNumber));
    }

    @И("^в контейнере \"(.+)\" в списке \"(.+)\" найти элемент № (.+)$")
    public void listItemFindByNumber(String containerName, String listName, String itemNumber) {
        super.listItemFindByNumber(eval(containerName), listName, eval(itemNumber));
    }

    @И("^в контейнере \"(.+)\" нажать на \"(.+)\" и скачать файл \"(.+)\"$")
    public void downloadFile(String containerName, String fileName, String buttonName) {
        super.downloadFile(eval(containerName), eval(fileName), buttonName);
    }

    @И("^в контейнере \"(.+)\" нажать на \"(.+)\" и выбрать загруженный файл \"(.+)\"$")
    public void attachFile(String containerName, String fileName, String buttonName) {
        super.attachFile(eval(containerName), eval(fileName), buttonName);
    }

    @И("^дождаться отображения контейнера \"(.+)\"$")
    public void waitForElement(String containerName) {
        super.waitForDisplayed(eval(containerName));
    }

    @И("^найти блок \"(.+)\", соответствующий условиям:$")
    public void findBlockByCondition(String containerName, DataTable dataTable) {
        super.findByCondition(eval(containerName), eval(dataTable));
    }

}
