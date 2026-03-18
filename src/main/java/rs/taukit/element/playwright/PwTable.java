package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.*;
import rs.taukit.exceptions.AutoTestBrokenException;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.exceptions.TableElementNotFoundException;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static rs.taukit.utils.BasicUtils.*;

public class PwTable extends PwBlock implements IHasTableElements {
    private PwBlock activeRow;
    private DataTable rowConditions;

    public PwTable(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwTable(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwTable(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwTable(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public int getHeaderIndex(String headerName) {
        List<String> headerNames = getHeaderNamesList();
        if (!headerNames.contains(headerName)) {
            throw TableElementNotFoundException.header(elementName, headerName, headerNames).get();
        }
        return headerNames.indexOf(headerName);
    }

    @Override
    public HasListItems getHeadersList() {
        return getDescendant("Заголовки");
    }

    @Override
    public List<String> getHeaderNamesList() {
        PwList headers = getDescendant("Заголовки");
        return headers.getItemList().stream()
                .map(HasText::getText)
                .toList();
    }

    @Override
    public IContainer getHeaderByName(String headerName) {
        return getHeadersList().getItemList().get(getHeaderIndex(headerName)).castTo(IContainer.class);
    }

    @Override
    public List<IContainer> getRowList() {
        PwList rowList = getDescendant("Строки");
        return rowList.getItemList().stream().map(row -> row.castTo(IContainer.class)).toList();
    }

    private Predicate<Map<String, String>> condition(PwList cells) {
        return (searchCondition) -> {
            String headerName = searchCondition.get("Столбец");
            String expectedValue = searchCondition.get("Значение");
            String cellContent = cells.get(getHeaderIndex(headerName)).castTo(HasText.class).getText();
            return cellContent.contains(expectedValue);
        };
    }

    private Predicate<PwList> conditions(DataTable dataTable) {
        return cells -> dataTable.asMaps().stream().allMatch(condition(cells));
    }

    private PwBlock getActiveRow() {
        page().setCurrentContext(activeRow);
        boolean conditionsMet = rowConditions.asMaps().stream()
                .allMatch(conditionData -> new TestCondition(conditionData).checkResult().isConditionMet());
        page().dropCurrentContext();
        if (!conditionsMet) {
            findRowByConditions(rowConditions);
        }
        return activeRow;
    }

    @Override
    public int findRowByConditions(DataTable dataTable) {
        scenarioData().put("контекст таблицы", this);
        this.rowConditions = dataTable;
        PwList rowList = getDescendant("Строки");
        IContainer currentContext = page().getCurrentContext();;
        int foundRowIndex = IntStream.range(0, rowList.getSize()).boxed().filter(index -> {
            try {
                activeRow = rowList.getItemList().get(index);
                page().setCurrentContext(activeRow);
                boolean conditionsMet = dataTable.asMaps().stream()
                        .allMatch(conditionData -> new TestCondition(conditionData).checkResult().isConditionMet());
                page().dropCurrentContext();
                return conditionsMet;
            } catch (Exception e) {
                if (scenarioData().getOptional("debug") != null){
                    TauLogger.console(e);
                }
                while (page().getCurrentContext() != currentContext) {
                    page().dropCurrentContext();
                }
                return false;
            }
        }).findFirst().orElse(-1);
        return foundRowIndex;
    }

    @Override
    public long countRowsByConditions(DataTable dataTable) {
        PwList rowList = getDescendant("Строки");
        return rowList.getItemList().stream().filter(row -> {
            Objects.requireNonNull(page()).setCurrentContext(row);
            boolean result = dataTable.asMaps().stream()
                    .allMatch(conditionData -> new TestCondition(conditionData).checkResult().isConditionMet());
            Objects.requireNonNull(page()).dropCurrentContext();
            return result;
        }).count();
    }

    @Override
    public IElement getActiveCell(String headerName) {
        if (activeRow.getElementByName("Столбцы").castTo(PwList.class).getItemList().size() <= getHeaderIndex(headerName)) {
            throw new AutoTestBrokenException("В строке %d нет столбца с номером %d (%s)",
                    activeRow.getCurrentIndex(), getHeaderIndex(headerName) + 1, headerName);
        }
        return activeRow.getElementByName("Столбцы").castTo(PwList.class).getItemList()
                .get(getHeaderIndex(headerName));
    }

    @Override
    public IElement getElementByConditions(String elementName, DataTable searchConditions) {
        try {
            PwElement expectedElement = descendants.get(elementName).castTo(PwElement.class);
            Locator elementLocator = expectedElement.getElementPwLocator();
            Locator foundElementLocator = descendants.get("Строки").castTo(PwList.class).getItemList().stream()
                    .map(row -> row.getElementByName("Столбцы").castTo(PwList.class))
                    .filter(conditions(searchConditions))
                    .findFirst().orElseThrow(TableElementNotFoundException.row(this.elementName))
                    .elementPwLocator.locator(elementLocator);
            return ElementBuilder.buildElement(elementName, expectedElement, foundElementLocator);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось найти элемент \"%s\" в таблице \"%s\"", elementName, this.elementName);
            return null;
        }
    }

    @Override
    public void clickOnElementByConditions(String elementName, DataTable conditions) {
        clickOnElementByConditions(elementName, conditions, false, null);
    }

    @Override
    @Step("в таблице '{this.elementName}' найти строку ({rowName}), соответствующую условиям:")
    public void findTableRowByConditions(String rowName, DataTable searchTerms) {
        findRowByConditions(searchTerms);
        scenarioData().put(rowName, this);
        TauLogger.attachScreenshot("Строка найдена", activeRow.elementPwLocator.screenshot());
    }

    @Override
    public DataTable tableRowSaveElementValues(DataTable dataTable) {
        List<List<String>> resultList = new ArrayList<>(dataTable.asLists().subList(0, 1));
        resultList.getFirst().add("Значение");
        dataTable.asMaps().forEach(data -> {
            String tableElement = TestDataUtils.get(data, "Объект");
            String elementName = TestDataUtils.get(data, "Название");
            String parameterName = data.getOrDefault("Параметр", elementName);
            String elementText;
            if (tableElement.equalsIgnoreCase("столбец")) {
                elementText = activeRow.getElementByName("Столбцы").castTo(PwList.class)
                        .get(getHeaderIndex(elementName)).castTo(HasText.class).getText();
                scenarioData().put(parameterName, elementText);
            } else {
                PwElement expectedElement = activeRow.getDescendant(elementName).castTo(PwElement.class);
                Locator expectedLocator = expectedElement.getElementPwLocator();
                elementText = ElementBuilder.buildElement(elementName, expectedElement, expectedLocator)
                        .castTo(HasText.class).getText();
                scenarioData().put(parameterName, elementText);
            }
            resultList.add(Arrays.asList(tableElement, elementName, parameterName, elementText));
        });
        return DataTable.create(resultList, TextUtils.tableConverter);
    }

    @Override
    public void tableRowElementClick(String rowName, String elementName) {
        PwElement expectedElement = descendants.get(elementName).castTo(PwElement.class);
        Locator expectedLocator = activeRow.elementPwLocator.locator(expectedElement.getElementPwLocator());
        ElementBuilder.buildElement(elementName, expectedElement, expectedLocator).click();
    }

    @Override
    public void setCheckbox(String checkboxName, boolean state) {
        PwCheckbox checkbox = getActiveRow().getElementByName(checkboxName).castTo(PwCheckbox.class);
        if (state) {
            checkbox.setCheckbox();
        } else {
            checkbox.unsetCheckbox();
        }
    }

    @Override
    public void setContext(String headerName) {
        PwList cells = getActiveRow().getElementByName("Столбцы").castTo(PwList.class);
        IContainer cell = cells.getItemList().get(getHeaderIndex(headerName)).castTo(IContainer.class);
        Objects.requireNonNull(page()).setCurrentContext(cell);
    }

    @Override
    public void setContainerContext(String containerName) {
        IContainer container = getActiveRow().getElementByName(containerName).castTo(IContainer.class);
        Objects.requireNonNull(page()).setCurrentContext(container);
    }

    @Override
    public void selectMenuItem(String menuName, String itemName) {
        getActiveRow().getElementByName(menuName).castTo(PwMenu.class).selectMenuItem(itemName);
    }

    @Override
    public IElement getMenuItem(String menuName, String itemName) {
        return getActiveRow().getElementByName(menuName).castTo(PwMenu.class).getMenuItem(itemName);
    }

    @Override
    public Downloadable getMenuDownloadableItem(String menuName, String itemName) {
        PwBlock element = getMenuItem(menuName, itemName).castTo(PwBlock.class);
        return new PwFileDownload(itemName, element.getElementPwLocator(), element.getModel());
    }

    @Override
    public boolean checkHeader(String name) {
        try {
            return getHeaderIndex(name) > -1;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void clickOnElementByConditions(String elementName, DataTable conditions, boolean needToSave, String parameterName) {
        TauLogger.attachDataTable("Условия выбора элемента", conditions);
        try {
            IElement foundElement = getElementByConditions(elementName, conditions);
            if (needToSave) {
                if (parameterName == null) {
                    parameterName = elementName;
                }
                scenarioData().put(parameterName, foundElement.castTo(HasText.class).getText());
            }
            foundElement.click();
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось нажать на элемент \"%s\" в таблице \"%s\"", elementName, this.elementName);
        }
    }

    public void clickOnElement(String elementName) {
        PwList headers = descendants.get("Заголовки").castTo(PwList.class);
        PwBlock activeRow = getActiveRow();
        if (activeRow.containsElement(elementName)) {
            activeRow.getElementByName(elementName).click();
        } else if (headers.contains(elementName)) {
            activeRow.getElementByName("Столбцы").castTo(PwList.class).get(getHeaderIndex(elementName)).click();
        } else {
            throw new ElementNotDescribedException(elementName, scenarioStorage().getCurrentPageName(), this.castTo(IHasTableElements.class));
        }
    }

    @Step("в таблице '{this.elementName}' нажать на '{elementName}', сохранив его значение, согласно условиям")
    public void saveAndClickOnElementByConditions(String elementName, DataTable conditions) {
        clickOnElementByConditions(elementName, conditions, true, null);
        TauLogger.report("Текст \"%s\" сохранён, как \"%s\"", scenarioData().get(elementName), elementName);
    }

    @Override
    public PwBlock setActiveRow(int index) {
        try {
            this.activeRow = getRowList().get(index).castTo(PwBlock.class);
            return this.activeRow;
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return null;
        }
    }

    @Override
    public int setActiveRowContext() {
        page().setCurrentContext(activeRow);
        return activeRow.getCurrentIndex();
    }

    @Override
    public void scroll(String direction) {
        double timeout = Config.getTimeProperty("conditions.wait.timeout");
        AtomicReference<Exception> exception = new AtomicReference<>();
        browser().getCurrentPage().waitForElement(elementName, true);
        BooleanSupplier condition = () -> {
            try {
                List<IContainer> rows = getRowList();
                int index = (int) (rows.stream().filter(CanBeVisible::isVisible).count() - 1);
                switch (direction) {
                    case ("вниз") -> rows.get(index).scrollIntoView(true);
                    case ("вверх") -> rows.getFirst().scrollIntoView(false);
                }
            } catch (Exception e) {
                exception.set(e);
                return false;
            }
            return true;
        };
        try {
            driver.waitForCondition(condition, timeout);
        } catch (Exception e) {
            e.addSuppressed(exception.get());
            throw e;
        }
    }
}
