package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
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
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static rs.taukit.utils.BasicUtils.scenarioData;
import static rs.taukit.utils.BasicUtils.testContext;

public class PwTable extends PwElement implements IHasTableElements {
    protected Locator headers;
    protected Locator rows;
    protected Locator cells;
    protected Map<String, IElement> elements;
    private Locator activeRow;

    public PwTable(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwTable(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwTable(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
        initFields(model);
    }

    public PwTable(String elementName, ElementModel model) {
        super(elementName, model);
        initFields(model);
    }

    private void initFields(ElementModel model) {
        this.headers = driver.getLocator(model.get("заголовки"));
        this.rows = driver.getLocator(model.get("строки"));
        this.cells = driver.getLocator(model.get("столбцы"));
        Map<String, Object> tableElementsMap = model.get("элементы", false);
        this.elements = tableElementsMap == null
                ? new HashMap<>()
                : ElementBuilder.buildElements(model.get("элементы"));
    }

    private List<String> headerList() {
        return element.locator(headers).all().stream().map(Locator::textContent).toList();
    }

    private int headerIndex(String headerName) {
        if (!headerList().contains(headerName)) {
            throw TableElementNotFoundException.header(elementName, headerName, headerList()).get();
        }
        return headerList().indexOf(headerName);
    }

    private Stream<Locator> rowStream() {
        return element.locator(rows).all().stream();
    }

    private Predicate<Map<String, String>> condition(Locator row) {
        return (searchCondition) -> {
            String headerName = searchCondition.get("Столбец");
            String expectedValue = searchCondition.get("Значение");
            String cellContent = row.locator(cells).all().get(headerIndex(headerName)).textContent();
            return cellContent.contains(expectedValue);
        };
    }

    private Predicate<Locator> conditions(DataTable dataTable) {
        return row -> dataTable.asMaps().stream().allMatch(condition(row));
    }

    @Override
    public int findRowByConditions(DataTable dataTable) {
        List<Locator> rowList = rowStream().toList();
        int rowIndex = IntStream.range(0, rowList.size())
                .filter(index -> conditions(dataTable).test(rowList.get(index))).findFirst()
                .orElseThrow(TableElementNotFoundException.row(elementName));
        activeRow = rowList.get(rowIndex);
        scenarioData().put("контекст таблицы", this);
        return rowIndex;
    }

    @Override
    public IElement getElementByConditions(String elementName, DataTable searchConditions) {
        try {
            PwElement expectedElement = elements.get(elementName).castTo(PwElement.class);
            Locator elementLocator = expectedElement.getElement();
            Locator foundElement = rowStream()
                    .filter(conditions(searchConditions))
                    .findFirst().orElseThrow(TableElementNotFoundException.row(this.elementName))
                    .locator(elementLocator);
            return ElementBuilder.buildElement(elementName, expectedElement, foundElement);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось найти элемент '%s' в таблице '%s'", elementName, this.elementName);
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
        activeRow = rowStream().filter(conditions(searchTerms)).findFirst()
                .orElseThrow(TableElementNotFoundException.row(rowName, elementName));
        scenarioData().put(rowName, this);
        TauLogger.attachScreenshot("Строка найдена", activeRow.screenshot());
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
                elementText = activeRow.locator(cells).all().get(headerIndex(elementName)).innerText();
                scenarioData().put(parameterName, elementText);
            } else {
                PwElement expectedElement = elements.get(elementName).castTo(PwElement.class);
                Locator expectedLocator = activeRow.locator(expectedElement.getElement());
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
        PwElement expectedElement = elements.get(elementName).castTo(PwElement.class);
        Locator expectedLocator = activeRow.locator(expectedElement.getElement());
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
            TauLogger.fail(e, "Не удалось нажать на элемент '%s' в таблице '%s'", elementName, this.elementName);
        }
    }

    public void clickOnElement(String elementName) {
        if (!elements.containsKey(elementName)) {
            throw new ElementNotDescribedException(elementName, testContext().getCurrentPageName());
        }
        element.locator(elements.get(elementName).getLocator()).click();
    }

    @Step("в таблице '{this.elementName}' нажать на '{elementName}', сохранив его значение, согласно условиям")
    public void saveAndClickOnElementByConditions(String elementName, DataTable conditions) {
        clickOnElementByConditions(elementName, conditions, true, null);
        TauLogger.report("Текст '%s' сохранён, как '%s'", scenarioData().get(elementName), elementName);
    }
}
