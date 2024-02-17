package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.element.interfaces.IHasText;
import rs.taukit.exceptions.ElementNotDescribedException;
import rs.taukit.exceptions.TableElementNotFoundException;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static rs.taukit.utils.BasicUtils.scenarioData;
import static rs.taukit.utils.BasicUtils.scenarioStorage;

public class PwTable extends PwElement implements IHasTableElements {
    private PwBlock activeRow;

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

    private int headerIndex(String headerName) {
        PwList headers = getDescendant("Заголовки");
        List<String> headerNames = headers.getItemList().stream()
                .map(PwElement::getText)
                .toList();
        if (!headers.contains(headerName)) {
            throw TableElementNotFoundException.header(elementName, headerName, headerNames).get();
        }
        return headerNames.indexOf(headerName);
    }

    private Predicate<Map<String, String>> condition(PwList cells) {
        return (searchCondition) -> {
            String headerName = searchCondition.get("Столбец");
            String expectedValue = searchCondition.get("Значение");
            String cellContent = cells.get(headerIndex(headerName)).getText();
            return cellContent.contains(expectedValue);
        };
    }

    private Predicate<PwList> conditions(DataTable dataTable) {
        return cells -> dataTable.asMaps().stream().allMatch(condition(cells));
    }

    @Override
    public int findRowByConditions(DataTable dataTable) {
        PwList rowList = getDescendant("Строки");
        AtomicReference<List<PwBlock>> itemList = new AtomicReference<>(rowList.getItemList());
        AtomicReference<Exception> exceptionAtomicReference = new AtomicReference<>();
        try {
            driver.waitForCondition(() -> {
                try {
                    itemList.set(rowList.getItemList());
                    return itemList.get().stream()
                            .anyMatch(item -> conditions(dataTable)
                                    .test(item.getElementByName("Столбцы").castTo(PwList.class)));
                } catch (Exception e) {
                    exceptionAtomicReference.set(e);
                    return false;
                }
            }, Config.getTimeProperty("element.check.interval"));
            int rowIndex = IntStream.range(0, itemList.get().size())
                    .filter(index -> conditions(dataTable)
                            .test(itemList.get().get(index).getElementByName("Столбцы").castTo(PwList.class)))
                    .findFirst().orElseThrow(TableElementNotFoundException.row(elementName));
            activeRow = itemList.get().get(rowIndex);
            scenarioData().put("контекст таблицы", this);
            return rowIndex;
        } catch (Exception e) {
            if (exceptionAtomicReference.get() != null) {
                e.addSuppressed(exceptionAtomicReference.get());
            }
            throw e;
        }
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
        dataTable.asMaps().forEach(data -> {
            String tableElement = TestDataUtils.get(data, "Объект");
            String elementName = TestDataUtils.get(data, "Название");
            String parameterName = data.getOrDefault("Параметр", elementName);
            String elementText;
            if (tableElement.equalsIgnoreCase("столбец")) {
                elementText = activeRow.getElementByName("Столбцы").castTo(PwList.class)
                        .get(headerIndex(elementName)).getText();
                scenarioData().put(parameterName, elementText);
            } else {
                PwElement expectedElement = descendants.get(elementName).castTo(PwElement.class);
                Locator expectedLocator = activeRow.elementPwLocator.locator(expectedElement.getElementPwLocator());
                elementText = ElementBuilder.buildElement(elementName, expectedElement, expectedLocator).getText();
                scenarioData().put(parameterName, elementText);
            }
            resultList.add(Arrays.asList(tableElement, elementName, elementText));
        });
        return DataTable.create(resultList, TextUtils.tableConverter);
    }

    @Override
    @Step("в строке ({rowName}) нажать на '{elementName}'")
    public void tableRowElementClick(String rowName, String elementName) {
        PwElement expectedElement = descendants.get(elementName).castTo(PwElement.class);
        Locator expectedLocator = activeRow.elementPwLocator.locator(expectedElement.getElementPwLocator());
        ElementBuilder.buildElement(elementName, expectedElement, expectedLocator).click();
    }

    private void clickOnElementByConditions(String elementName, DataTable conditions, boolean needToSave, String parameterName) {
        TauLogger.attachDataTable("Условия выбора элемента", conditions);
        try {
            IElement foundElement = getElementByConditions(elementName, conditions);
            if (needToSave) {
                if (parameterName == null) {
                    parameterName = elementName;
                }
                scenarioData().put(parameterName, foundElement.castTo(IHasText.class).getText());
            }
            foundElement.click();
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось нажать на элемент \"%s\" в таблице \"%s\"", elementName, this.elementName);
        }
    }

    public void clickOnElement(String elementName) {
        PwList headers = descendants.get("Заголовки").castTo(PwList.class);
        if (activeRow.containsElement(elementName)) {
            activeRow.getElementByName(elementName).click();
        } else if (headers.contains(elementName)) {
            activeRow.getElementByName("Столбцы").castTo(PwList.class).get(headerIndex(elementName)).click();
        } else {
            throw new ElementNotDescribedException(elementName, scenarioStorage().getCurrentPageName(), this);
        }
    }

    @Step("в таблице '{this.elementName}' нажать на '{elementName}', сохранив его значение, согласно условиям")
    public void saveAndClickOnElementByConditions(String elementName, DataTable conditions) {
        clickOnElementByConditions(elementName, conditions, true, null);
        TauLogger.report("Текст \"%s\" сохранён, как \"%s\"", scenarioData().get(elementName), elementName);
    }
}
