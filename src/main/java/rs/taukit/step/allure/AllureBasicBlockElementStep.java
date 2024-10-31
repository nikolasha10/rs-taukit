package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.*;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class AllureBasicBlockElementStep extends BasicUtils {
    private final String blockName;
    private IHasBlockElements blockElements;

    public AllureBasicBlockElementStep(String blockElementName) {
        this.blockName = blockElementName;
        String[] blockHierarchy = blockElementName.split(" -> ");
        IHasBlockElements currentContext = page().getCurrentContext();
        try {
            driver().waitForCondition(currentContext::isVisible);
            IHasBlockElements container = page().getElement(blockHierarchy[0]).castTo(IHasBlockElements.class);
            for (int i = 1; i < blockHierarchy.length; i++) {
                driver().waitForCondition(container::isVisible);
                container = container.getElementByName(blockHierarchy[i]).castTo(IHasBlockElements.class);
            }
            driver().waitForCondition(container::isVisible);
            this.blockElements = container;
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться блока '%s'", blockElementName);
        }
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
        scenarioStorage().saveStepAction(runnable);
        try {
            runnable.run();
        } catch (Exception e){
            TauLogger.fail(e, "Не удалось нажать на %s '%s' в блоке '%s' на странице '%s'",
                    elementType, fieldName, blockName, page().getPageName());
        }
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
        TauLogger.report("В блоке \"%s\" Нажата кнопка \"%s\"", blockName, buttonName);
        TauLogger.report("Выбран файл \"%s\" из папки \"%s\"", fileName, dirPath);
    }


    @Step("дождаться отображения блока '{this.blockName}'")
    public void waitForDisplayed() {
        try {
            driver().waitForCondition(blockElements::isVisible);
            TauLogger.report("Блок \"%s\" отображается", blockName);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось дождаться блока \"%s\" на странице \"%s\"",
                    blockName, scenarioStorage().getCurrentPageName());
        }

    }

    @Step("найти блок '{this.blockName}', соответствующий условиям:")
    public void findByCondition(DataTable dataTable) {
        TauLogger.attachDataTable("Условия поиска блока", dataTable);
        page().setCurrentContext(blockName);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<IHasBlockElements> foundElement = new AtomicReference<>();
        AtomicReference<ExpectedResult> result = new AtomicReference<>();
        BooleanSupplier isFound = () -> {
            foundElement.set(blockElements.getStream().filter(element -> {
                page().replaceCurrentContext(element);
                element.setCurrentIndex(counter.getAndAdd(1));
                return dataTable.asMaps().stream()
                        .map(data -> new ExpectedResult(data).waitForResult())
                        .peek(result::set)
                        .allMatch(ExpectedResult::isConditionMet);
            }).findFirst().orElse(null));
            return foundElement.get() != null;
        };
        try {
            driver().waitForCondition(isFound);
            TauLogger.report("[примечание] текущий контекст: блок элементов '%s' (%d)",
                    blockName, foundElement.get().getCurrentIndex() + 1);
        } catch (Exception e) {
            if (result.get().getThrownException() != null) {
                e.addSuppressed(result.get().getThrownException());
            }
            TauLogger.fail(e, "Не удалось найти блок '%s', соответствующий заданным условиям", blockName);
        }
    }

    @Step("в блоке '{this.blockName}' установить переключатель '{fieldName}' в значение '{value}'")
    public void setRadiButton(String fieldName, String value) {
        IHasEditableValue editable = blockElements.getElementByName(fieldName).castTo(IHasEditableValue.class);
        editable.setValue(value);
        TauLogger.report("Переключатель '%s' в блоке '%s' установлен в значение '%s'", fieldName, this.blockName, value);
    }
}
