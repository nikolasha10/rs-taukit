package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.*;
import rs.taukit.testdata.ExpectedResult;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

public class ContainerManager extends StepManager {

    private IContainer getContainer(String containerName) {
        double timeout = Config.getTimeProperty("element.check.timeout");
        String[] blockHierarchy = containerName.split(" -> ");
        IContainer currentContext = getPage().getCurrentContext();
        IWebDriver driver = getDriver();
        driver.waitForCondition(currentContext::isVisible, timeout);
        IContainer container = getPage().getElement(blockHierarchy[0]).castTo(IContainer.class);
        for (int i = 1; i < blockHierarchy.length; i++) {
            driver.waitForCondition(container::isVisible, timeout);
            container = container.getElementByName(blockHierarchy[i]).castTo(IContainer.class);
        }
        driver.waitForCondition(container::isVisible, timeout);
        return container;
    }

    @Step("в контейнере '{containerName}' заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String containerName, String fieldName, String value) {
        run(() -> {
            IContainer container = getContainer(containerName);
            IHasEditableValue editableValue = container.getElementByName(fieldName).castTo(IHasEditableValue.class);
            editableValue.setValue(value);
        }, "Не удалось заполнить поле '%s' в контейнере '%s' значением '%s'", fieldName, containerName, value);
        TauLogger.report("Поле '%s' в контейнере '%s' заполнено значением '%s'", fieldName, containerName, value);
    }

    @Step("в контейнере '{containerName}' заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String containerName, String fieldName, String value, String controlValue) {
        run(() -> {
            IContainer container = getContainer(containerName);
            IHasEditableValue editableValue = container.getElementByName(fieldName).castTo(IHasEditableValue.class);
            editableValue.setValue(value, controlValue);
        }, "Не удалось заполнить поле '%s' в контейнере '%s' значением '%s'", fieldName, containerName, value);
        TauLogger.report("Поле '%s' в контейнере '%s' заполнено значением '%s'", fieldName, containerName, value);
    }

    @Step("сохранить значения поля '{fieldName}' в контейнере '{containerName}', как '{parameterName}'")
    public void saveElementValue(String fieldName, String containerName, String parameterName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    IHasEditableValue editableValue = container.getElementByName(fieldName).castTo(IHasEditableValue.class);
                    String value = editableValue.getText();
                    scenarioData.put(parameterName, value);
                }, "Не удалось сохранить значение поля '%s' в контейнере '%s', как '%s'",
                fieldName, containerName, parameterName);
        TauLogger.report("Значение поля '%s' в контейнере '%s' сохранено, как '%s'",
                fieldName, containerName, parameterName);
    }

    @Step("в контейнере '{containerName}' нажать на {elementType} '{fieldName}'")
    public void clickOnField(String containerName, String elementType, String fieldName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    IElement iElement = container.getElementByName(fieldName).castTo(IElement.class);
                    getDriver().waitForCondition(iElement::isVisible);
                    iElement.click();
                }, "Не удалось нажать на %s '%s' в контейнере '%s' на странице '%s'",
                elementType, fieldName, containerName, getPage().getPageName());
        TauLogger.report("Нажали на %s '%s' в контейнере '%s'", elementType, fieldName, containerName);
    }

    @Step("в контейнере '{containerName}' установить чекбокс '{fieldName}'")
    public void setCheckbox(String containerName, String fieldName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    IHasCheckbox checkbox = container.getElementByName(fieldName).castTo(IHasCheckbox.class);
                    checkbox.setCheckbox();
                }, "Не удалось установить чекбокс '%s' в контейнере '%s' на странице '%s'",
                fieldName, containerName, getPage().getPageName());
        TauLogger.report("Установлен чекбокс '%s' в контейнере '%s'", fieldName, containerName);
    }

    @Step("в контейнере '{containerName}' в списке '{listName}' выбрать '{itemName}'")
    public void listValueClick(String containerName, String listName, String itemName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    IHasEditableValue editable = container.getElementByName(listName).castTo(IHasEditableValue.class);
                    editable.setValue(itemName);
                }, "Не удалось выбрать '%s' в списке '%s' в контейнере '%s' на странице '%s'",
                itemName, listName, containerName, getPage().getPageName());
        TauLogger.report("В контейнере '%s' в списке '%s' выбрано значение '%s'", containerName, listName, itemName);
    }

    @Step("в контейнере '{containerName}' нажать на '{buttonName}' и скачать файл '{fileName}'")
    public void downloadFile(String containerName, String buttonName, String fileName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    IHasDownloadableFile downloadable = container.getElementByName(buttonName)
                            .castTo(IHasDownloadableFile.class);
                    downloadable.click(fileName);
                }, "Не удалось скачать файл '%s' по нажатию на '%s' в контейнере '%s' на странице '%s'",
                fileName, buttonName, containerName, getPage().getPageName());
        TauLogger.report("По нажатию на '%s' в контейнере '%s' был загружен файл '%s'", buttonName, containerName, fileName);
    }

    @Step("в контейнере '{containerName}' нажать на '{buttonName}' и выбрать загруженный файл '{fileName}'")
    public void attachFile(String containerName, String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        Runnable runnable = () -> {
            IContainer container = getContainer(containerName);
            IHasAttachment attachment = container.getElementByName(buttonName).castTo(IHasAttachment.class);
            attachment.attach(dirPath, fileName);
        };
        run(runnable, "Не удалось нажать на '%s' в контейнере '%s' и выбрать загруженный файл '%s'",
                buttonName, containerName, fileName);
        TauLogger.report("В контейнере '%s' нажата кнопка '%s'", containerName, buttonName);
        TauLogger.report("Выбран файл '%s' из папки '%s'", fileName, dirPath);
    }


    @Step("дождаться отображения контейнера '{containerName}'")
    public void waitForDisplayed(String containerName) {
        BooleanSupplier condition = () -> {
            try {
                return getContainer(containerName).isVisible();
            } catch (Exception e) {
                return false;
            }
        };
        waitForCondition(condition, "Не удалось дождаться отображения контейнера '%s' на странице '%s'",
                containerName, getPage().getPageName());
        TauLogger.report("Контейнер '%s' отображается", containerName);
    }

    private IContainer getContainerByCondition(IContainer container, DataTable dataTable,
                                               AtomicInteger index, AtomicReference<Exception> e) {
        AtomicReference<ExpectedResult> result = new AtomicReference<>();
        IContainer found = container.getStream().filter(element -> {
            getPage().replaceCurrentContext(element);
            element.setCurrentIndex(index.getAndAdd(1));
            return dataTable.asMaps().stream()
                    .map(data -> new ExpectedResult(data).waitForResult())
                    .peek(result::set)
                    .allMatch(ExpectedResult::isConditionMet);
        }).findFirst().orElse(null);
        if (result.get().getThrownException() != null) {
            e.set(result.get().getThrownException());
        }
        return found;
    }

    @Step("найти контейнер '{containerName}', соответствующий условиям:")
    public void findByCondition(String containerName, DataTable dataTable) {
        TauLogger.attachDataTable("Условия поиска блока", dataTable);
        AtomicInteger index = new AtomicInteger(0);
        AtomicReference<Exception> exception = new AtomicReference<>();
        run(() -> {
            IContainer container = getContainer(containerName);
            getPage().setCurrentContext(containerName);
            try {
                getDriver().waitForCondition(() -> getContainerByCondition(container, dataTable, index, exception) != null);
            } catch (Exception e) {
                throw new RuntimeException(exception.get() != null ? exception.get() : e);
            }
        }, "Не удалось найти контейнер '%s', соответствующий заданным условиям", containerName);
        TauLogger.report("[примечание] текущий контекст: контейнер '%s' (%d)", containerName, index.get() + 1);

    }

    @Step("в контейнере '{containerName}' установить переключатель '{fieldName}' в значение '{value}'")
    public void setRadiButton(String containerName, String fieldName, String value) {
        run(() -> {
            IContainer container = getContainer(containerName);
            IHasEditableValue editable = container.getElementByName(fieldName).castTo(IHasEditableValue.class);
            editable.setValue(value);
        }, "Не удалось установить переключатель '%s' в контейнере '%s' в значение '%s'", fieldName, containerName, value);

        TauLogger.report("Переключатель '%s' в контейнере '%s' установлен в значение '%s'", fieldName, containerName, value);
    }
}
