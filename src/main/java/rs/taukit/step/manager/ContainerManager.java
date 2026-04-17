package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.driver.IWebDriver;
import rs.taukit.element.interfaces.*;
import rs.taukit.testobject.TestCondition;
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
            Editable editableValue = container.getElementByName(fieldName).castTo(Editable.class);
            editableValue.setValue(value);
            TauLogger.console("Поле '%s' в контейнере '%s' заполнено значением '%s'", fieldName, containerName, value);
        }, "Не удалось заполнить поле '%s' в контейнере '%s' значением '%s'", fieldName, containerName, value);
    }

    @Step("в контейнере '{containerName}' заполнить поле '{fieldName}' значением '{value}'")
    public void setValue(String containerName, String fieldName, String value, String controlValue) {
        run(() -> {
            IContainer container = getContainer(containerName);
            Editable editableValue = container.getElementByName(fieldName).castTo(Editable.class);
            editableValue.setValue(value, controlValue);
        }, "Не удалось заполнить поле '%s' в контейнере '%s' значением '%s'", fieldName, containerName, value);
        TauLogger.console("Поле '%s' в контейнере '%s' заполнено значением '%s'", fieldName, containerName, value);
    }

    @Step("в контейнере '{containerName}' очистить поле '{fieldName}'")
    public void clear(String containerName, String fieldName) {
        run(() -> {
            IContainer container = getContainer(containerName);
            Editable editableValue = container.getElementByName(fieldName).castTo(Editable.class);
            editableValue.clear();
        }, "Не удалось очистить поле '%s' в контейнере '%s'", fieldName, containerName);
        TauLogger.console("Поле '%s' в контейнере '%s' очищено", fieldName, containerName);
    }

    @Step("сохранить значения поля '{fieldName}' в контейнере '{containerName}', как '{parameterName}'")
    public void saveElementValue(String containerName, String fieldName, String parameterName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    HasText hasText = container.getElementByName(fieldName).castTo(HasText.class);
                    String value = hasText.getText();
                    scenarioData.put(parameterName, value);
                }, "Не удалось сохранить значение поля '%s' в контейнере '%s', как '%s'",
                fieldName, containerName, parameterName);
        TauLogger.console("Значение поля '%s' в контейнере '%s' сохранено, как '%s': %s",
                fieldName, containerName, parameterName, scenarioData.get(parameterName));
        TauLogger.attach(parameterName, scenarioData.get(parameterName));
    }

    @Step("в контейнере '{containerName}' нажать на {elementType} '{fieldName}'")
    public void clickOnField(String containerName, String elementType, String fieldName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    IElement iElement = container.getElementByName(fieldName).castTo(IElement.class);
                    getDriver().waitForCondition(iElement::isVisible);
                    iElement.click();
                    TauLogger.report("Нажали на %s '%s' в контейнере '%s'", elementType, fieldName, containerName);
                }, "Не удалось нажать на %s '%s' в контейнере '%s' на странице '%s'",
                elementType, fieldName, containerName, getPage().getPageName());
    }

    @Step("в контейнере '{containerName}' установить чекбокс '{fieldName}'")
    public void setCheckbox(String containerName, String fieldName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    CanBeChecked checkbox = container.getElementByName(fieldName).castTo(CanBeChecked.class);
                    checkbox.setCheckbox();
                }, "Не удалось установить чекбокс '%s' в контейнере '%s' на странице '%s'",
                fieldName, containerName, getPage().getPageName());
        TauLogger.console("Установлен чекбокс '%s' в контейнере '%s'", fieldName, containerName);
    }

    @Step("в контейнере '{containerName}' снять чекбокс '{fieldName}'")
    public void unsetCheckbox(String containerName, String fieldName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    CanBeChecked checkbox = container.getElementByName(fieldName).castTo(CanBeChecked.class);
                    checkbox.unsetCheckbox();
                }, "Не удалось снять чекбокс '%s' в контейнере '%s' на странице '%s'",
                fieldName, containerName, getPage().getPageName());
        TauLogger.console("Снят чекбокс '%s' в контейнере '%s'", fieldName, containerName);
    }

    @Step("в контейнере '{containerName}' в списке '{listName}' выбрать '{itemName}'")
    public void listValueClick(String containerName, String listName, String itemName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    HasListItems listItems = container.getElementByName(listName).castTo(HasListItems.class);
                    listItems.selectItem(itemName);
                }, "Не удалось выбрать '%s' в списке '%s' в контейнере '%s' на странице '%s'",
                itemName, listName, containerName, getPage().getPageName());
        TauLogger.console("В контейнере '%s' в списке '%s' выбрано значение '%s'", containerName, listName, itemName);
    }

    @Step("в контейнере '{containerName}' в списке '{listName}' найти элемент с текстом '{itemValue}'")
    public void listItemFindByValue(String containerName, String listName, String itemValue) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    HasListItems listItems = container.getElementByName(listName).castTo(HasListItems.class);
                    IContainer itemContainer = listItems.getItemList().stream()
                            .filter(item -> item.castTo(HasText.class).getText().equalsIgnoreCase(itemValue))
                            .findFirst().orElseThrow().castTo(IContainer.class);
                    getPage().setCurrentContext(itemContainer);
                }, "Не удалось найти элемент с текстом '%s' в списке '%s' в контейнере '%s' на странице '%s'",
                itemValue, listName, containerName, getPage().getPageName());
        TauLogger.report("[примечание] текущий контекст - элемент с тестом '%s' в списке '%s' в контейнере '%s'",
                itemValue, listName, containerName);
    }

    @Step("в контейнере '{containerName}' в списке '{listName}' выбрать элемент № {itemNumber}")
    protected void listItemClickByNumber(String containerName, String listName, String itemNumber) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    HasListItems listItems = container.getElementByName(listName).castTo(HasListItems.class);
                    int index = Integer.parseInt(itemNumber) - 1;
                    listItems.getItemList().get(index).click();
                }, "Не удалось выбрать элемент № %s в списке '%s' в контейнере '%s' на странице '%s'",
                itemNumber, listName, containerName, getPage().getPageName());
        TauLogger.console("В контейнере '%s' в списке '%s' выбран элемент № %s", containerName, listName, itemNumber);
    }

    @Step("в контейнере '{containerName}' в списке '{listName}' выбрать элемент № {itemNumber}")
    protected void listItemFindByNumber(String containerName, String listName, String itemNumber) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    HasListItems listItems = container.getElementByName(listName).castTo(HasListItems.class);
                    int index = Integer.parseInt(itemNumber) - 1;
                    IContainer item = listItems.getItemList().get(index).castTo(IContainer.class);
                    getPage().setCurrentContext(item);
                }, "Не удалось найти элемент № %s в списке '%s' в контейнере '%s' на странице '%s'",
                itemNumber, listName, containerName, getPage().getPageName());
        TauLogger.report("[примечание] текущий контекст - элемент № %s в списке '%s' в контейнере '%s'",
                itemNumber, listName, containerName);
    }

    @Step("в контейнере '{containerName}' нажать на '{buttonName}' и скачать файл '{fileName}'")
    public void downloadFile(String containerName, String buttonName, String fileName) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    IContainer currentContext = getPage().getCurrentContext();
                    getPage().setCurrentContext(container);
                    FileManager fileManager = new FileManager();
                    fileManager.init("", fileName);
                    fileManager.downloadFile(buttonName);
                    getPage().setCurrentContext(currentContext);
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
        AtomicReference<TestCondition> result = new AtomicReference<>();
        IContainer found = container.getStream().filter(element -> {
            getPage().replaceCurrentContext(element);
            element.setCurrentIndex(index.getAndAdd(1));
            return dataTable.asMaps().stream()
                    .map(data -> new TestCondition(data).waitForResult())
                    .peek(result::set)
                    .allMatch(TestCondition::isConditionMet);
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
    public void setRadioButton(String containerName, String fieldName, String value) {
        run(() -> {
            IContainer container = getContainer(containerName);
            Editable editable = container.getElementByName(fieldName).castTo(Editable.class);
            editable.setValue(value);
        }, "Не удалось установить переключатель '%s' в контейнере '%s' в значение '%s'", fieldName, containerName, value);

        TauLogger.report("Переключатель '%s' в контейнере '%s' установлен в значение '%s'", fieldName, containerName, value);
    }

    @Step("в контейнере '{containerName}' в меню '{menuName}' выбрать '{itemName}'")
    protected void selectMenuItem(String containerName, String menuName, String itemName) {
        run(() -> {
            IContainer container = getContainer(containerName);
            IHasMenu menu = container.getElementByName(menuName).castTo(IHasMenu.class);
            menu.selectMenuItem(itemName);
        }, "Не удалось выбрать пункт '%s' в меню '%s' в контейнере '%s'", itemName, menuName, containerName);
        TauLogger.console("Выбран пункт '%s' в меню '%s' в контейнере '%s'", itemName, menuName, containerName);
    }

    @Step("в контейнере '{containerName}' в выпадающем списке '{dropdownListName}' выбрать '{value}'")
    protected void selectDropdownListItem(String containerName, String dropdownListName, String value) {
        AtomicReference<RuntimeException> exception = new AtomicReference<>();
        BooleanSupplier condition = () -> {
            try {
                IContainer container = getContainer(containerName);
                Editable dropdownList = container.getElementByName(dropdownListName).castTo(Editable.class);
                dropdownList.setValue(value);
                return true;
            } catch (RuntimeException e) {
                exception.set(e);
                return false;
            }
        };
        waitForCondition(condition, exception.get(),
                "Не удалось выбрать элемент '%s' в выпадающем списке '%s' в контейнере '%s'",
                value, dropdownListName, containerName);
        TauLogger.console("Выбран элемент '%s' в выпадающем списке '%s' в контейнере '%s'",
                value, dropdownListName, containerName);
    }

    @Step("в контейнере '{containerName}' в выпадающем списке '{dropdownListName}' выбрать {number}-й элемент")
    protected void selectDropdownListItem(String containerName, String dropdownListName, int number) {
        run(() -> {
                    IContainer container = getContainer(containerName);
                    Selectable dropdown = container.getElementByName(dropdownListName).castTo(Selectable.class);
                    dropdown.getOptionByNumber(number).click();
                    TauLogger.console("В контейнере '%s' в выпадающем списке '%s' выбран %d-й элемент",
                            containerName, dropdownListName, number);
                },
                "Не удалось выбрать %d-й элемент в выпадающем списке '%s' в контейнере '%s'",
                number, dropdownListName, containerName);
    }
}
