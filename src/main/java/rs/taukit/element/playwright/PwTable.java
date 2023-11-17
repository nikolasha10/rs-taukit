package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.element.interfaces.IHasText;
import rs.taukit.exceptions.ElementNotBuiltException;
import rs.taukit.exceptions.TableElementNotFoundException;
import rs.taukit.page.playwright.ElementBuilder;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.TextUtils;
import rs.taukit.utils.YamlParser;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static rs.taukit.step.utils.BasicUtils.scenarioData;
import static rs.taukit.step.utils.BasicUtils.testContext;

public class PwTable extends PwElement implements IHasTableElements {
    protected final Locator headers;
    protected final Locator rows;
    protected final Locator cells;
    protected final Map<String, IElement> elements;
    private Locator activeRow;

    public PwTable(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
        this.headers = driver.getLocator(model.get("заголовки"));
        this.rows = driver.getLocator(model.get("строки"));
        this.cells = driver.getLocator(model.get("столбцы"));
        Map<String, String> elements = model.get("элементы");
//        this.elements = buildElements(elements);
        this.elements = ElementBuilder.buildElements(elements);
    }

    private List<String> headerList() {
        return element.locator(headers).all().stream().map(Locator::textContent).toList();
    }

    private int headerIndex(String headerName) {
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
    public IElement getElementByConditions(String elementName, DataTable searchConditions) {
        try {
            PwElement expectedElement = elements.get(elementName).castTo(PwElement.class);
            Locator elementLocator = expectedElement.getElement();
            Locator foundElement = rowStream()
                    .filter(conditions(searchConditions))
                    .findFirst().orElseThrow()
                    .locator(elementLocator);
            return ElementBuilder.buildElement(elementName, expectedElement, foundElement);
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось найти элемент '%s' в таблице '%s'", elementName, this.elementName);
            return null;
        }
    }

    @Override
    @Step("в таблице '{this.elementName}' нажать на '{elementName}' согласно условиям")
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
    @Step("в строке ({rowName}) найти и сохранить значения элементов:")
    public void tableRowSaveElementValues(String rowName, DataTable dataTable) {
        List<List<String>> resultList = new ArrayList<>(dataTable.asLists().subList(0, 0));
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
        TauLogger.attachDataTable("Значения элементов", DataTable.create(resultList, TextUtils.tableConverter));
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
            TauLogger.fail(e, "Не удалось нажать на элемент '%s' в таблице '%s'",
                    elementName, this.elementName);
        }
    }

    @Step("в таблице '{this.elementName}' нажать на '{elementName}', сохранив его значение, согласно условиям")
    public void saveAndClickOnElementByConditions(String elementName, DataTable conditions) {
        clickOnElementByConditions(elementName, conditions, true, null);
        TauLogger.report("Текст '%s' сохранён, как '%s'", scenarioData().get(elementName), elementName);
    }

//    @SuppressWarnings("unchecked")
//    private Map<String, IElement> buildElements(Map<String, ?> elements) {
//        Map<String, IElement> result = new HashMap<>();
//        elements.forEach((elementName, elementObject) -> {
//            if (elementObject instanceof String elementLocator) {
//                result.put(elementName, buildElement(elementName, elementLocator));
//            } else if (elementObject instanceof Map subElementData) {
//                IElement element = buildElement(elementName, subElementData);
//                result.put(elementName, element);
//            }
//        });
//        return result;
//    }
//
//    private IElement buildElement(String elementName, String locator) {
//        return new PwElement(elementName, locator);
//    }
//
//    private IElement buildElement(PwElement element, Locator locator) {
//        Class<? extends IElement> elementClass = element.getClass();
//        try {
//            return elementClass.getConstructor(String.class, Locator.class).newInstance(elementName, locator);
//        } catch (Exception e) {
//            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), element.getElementData().toString());
//        }
//    }
//
//    private IElement buildElement(String elementName, Map<String, String> elementData) {
//        String elementTypeName = model.get("Тип");
//        Class<? extends IElement> elementClass = ElementType.getType(elementTypeName).getElementClass();
//        return buildElement(elementName, elementClass, elementData);
//    }
//
//    private IElement buildElement(String elementName, Class<? extends IElement> elementClass, Map<String, String> elementData) {
//        try {
//            return elementClass.getConstructor(String.class, model.getClass())
//                    .newInstance(elementName, elementData);
//        } catch (Exception e) {
//            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), elementData.toString());
//        }
//
//    }

}
