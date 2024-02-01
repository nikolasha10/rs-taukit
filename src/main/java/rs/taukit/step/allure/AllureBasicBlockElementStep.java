package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.*;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicBlockElementStep extends BasicUtils {
    private final String blockName;
    private final IHasBlockElements blockElements;

    public AllureBasicBlockElementStep(String blockElementName) {
        this.blockName = blockElementName;
        Class<IElement> elementType = ElementType.getType("блок элементов").getElementClass();
        String[] elements = blockElementName.split(" -> ");
        IHasBlockElements element = page().replaceElement(elements[0], elementType).castTo(IHasBlockElements.class);
        for (int i = 1; i < elements.length; i++) {
            element = element.getElementByName(1, elements[i]).castTo(IHasBlockElements.class);
        }
        this.blockElements = element;
        driver().waitForCondition(this.blockElements::isVisible);
    }

    @Step("в блоке '{this.blockName}' заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String fieldName, String value) {
        blockElements.getElementByName(fieldName).castTo(IHasEditableValue.class).setValue(value);
        TauLogger.report("Поле \"%s\" в блоке \"%s\" заполнено значением \"%s\"", fieldName, blockName, blockElements.getText());
    }

    @Step("в блоке '{this.blockName}' заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String fieldName, String value, String controlValue) {
        blockElements.getElementByName(fieldName).castTo(IHasEditableValue.class).setValue(value, controlValue);
        TauLogger.report("Поле \"%s\" в блоке \"%s\" заполнено значением \"%s\"", fieldName, blockName, blockElements.getText());
    }

    @Step("сохранить значения поля '{fieldName}' в блоке '{this.blockName}', как '{parameterName}'")
    public void saveElementValue(String fieldName, String parameterName) {
        scenarioData().put(parameterName, blockElements.getElementByName(fieldName).castTo(IHasEditableValue.class).getText());
    }

    @Step("в блоке '{this.blockName}' нажать на {elementType} '{fieldName}'")
    public void clickOnField(String elementType, String fieldName) {
        Runnable runnable = () -> {
            IElement iElement = blockElements.getElementByName(fieldName).castTo(IElement.class);
            driver().waitForCondition(iElement::isVisible);
            iElement.click();
            TauLogger.report("Нажали на %s \"%s\" в блоке \"%s\"", elementType, fieldName, blockName);
        };
        testContext().saveStepAction(runnable);
        runnable.run();
    }

    @Step("в блоке '{this.blockName}' установить чекбокс '{fieldName}'")
    public void setCheckbox(String fieldName) {
        blockElements.getElementByName(fieldName).castTo(IHasCheckbox.class).setCheckbox();
        TauLogger.report("Установлен чекбокс \"%s\" в блоке \"%s\"", fieldName, blockName);
    }

    @Step("в блоке '{this.blockName}' в списке '{listName}' выбрать '{itemName}'")
    public void listValueClick(String listName, String itemName) {
        blockElements.getElementByName(listName).castTo(IHasEditableValue.class).setValue(itemName);
        TauLogger.report("В блоке \"%s\" в списке \"%s\" выбрано значение \"%s\"", blockName, listName, itemName);
    }

    @Step("в блоке '{this.blockName}' нажать на '{buttonName}' и скачать файл '{fileName}'")
    public void downloadFile(String buttonName, String fileName) {
        blockElements.getElementByName(buttonName).castTo(IHasDownloadableFile.class).click(fileName);
    }

    @Step("в блоке '{this.blockName}' нажать на '{buttonName}' и выбрать загруженный файл '{fileName}'")
    public void attachFile(String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        blockElements.getElementByName(buttonName).castTo(IHasAttachment.class).attach(dirPath, fileName);
        TauLogger.report("Нажата кнопка \"%s\"", buttonName);
        TauLogger.report("Выбран файл \"%s\" из папки \"%s\"", fileName, dirPath);
    }


    @Step("дождаться отображения блока '{this.blockName}'")
    public void waitForDisplayed() {
        try {
            driver().waitForCondition(blockElements::isVisible);
            TauLogger.report("Блок \"%s\" отображается", blockName);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться блока \"%s\" на странице \"%s\"",
                    blockName, testContext().getCurrentPageName());
        }

    }
}
