package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.ElementType;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.element.interfaces.IHasTableElements;
import rs.taukit.element.interfaces.IHasText;
import rs.taukit.exceptions.ElementNotBuiltException;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.YamlParser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static rs.taukit.step.utils.BasicUtils.scenarioData;
import static rs.taukit.step.utils.BasicUtils.testContext;

public class ElementTable extends PlaywrightElement implements IHasTableElements {
    protected final Locator headers;
    protected final Locator rows;
    protected final Locator cells;
    protected final Map<String, IElement> elements;

    public ElementTable(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName, elementData);
        this.headers = driver.getLocator(elementData.get("Заголовки"));
        this.rows = driver.getLocator(elementData.get("Строки"));
        this.cells = driver.getLocator(elementData.get("Столбцы"));
        Map<String, String> elements = YamlParser.convert(elementData.get("Элементы"));
        this.elements = buildElements(elements);
    }

    @Override
    public IElement getElementByConditions(String elementName, DataTable conditions) {
        List<String> headerList = element.locator(headers).all().stream().map(Locator::textContent).toList();
        try {
            PlaywrightElement expectedElement = elements.get(elementName).castTo(PlaywrightElement.class);
            Locator elementLocator = expectedElement.castTo(PlaywrightElement.class).getElement();
            Locator foundElement = element.locator(rows).all().stream()
                    .filter(row -> conditions.asMaps().stream()
                            .allMatch(condition -> {
                                String headerName = condition.get("Столбец");
                                int headerIndex = headerList.indexOf(headerName);
                                String cellContent = row.locator(cells).all().get(headerIndex).textContent();
                                String expectedValue = condition.get("Значение");
                                return cellContent.contains(expectedValue);
                            }))
                    .findFirst().orElseThrow()
                    .locator(elementLocator);
            return buildElement(expectedElement, foundElement);
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

    private void clickOnElementByConditions(String elementName, DataTable conditions, boolean needToSave, String parameterName) {
        TauLogger.attachDataTable("Условия выбора элемента", conditions);
        try {
            IElement foundElement = getElementByConditions(elementName, conditions);
            if (needToSave) {
                if (parameterName == null) {
                    parameterName = elementName;
                }
                testContext().save(parameterName, foundElement.castTo(IHasText.class).getText());
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

    @SuppressWarnings("unchecked")
    private Map<String, IElement> buildElements(Map<String, ?> elements) {
        Map<String, IElement> result = new HashMap<>();
        elements.forEach((elementName, elementObject) -> {
            if (elementObject instanceof String elementLocator){
                result.put(elementName, buildElement(elementName, elementLocator));
            } else if (elementObject instanceof Map subElementData){
                IElement element = buildElement(elementName, subElementData);
                result.put(elementName, element);
            }
        });
        return result;
    }

    private IElement buildElement(String elementName, String locator) {
        return new PlaywrightElement(elementName, locator);
    }
    private IElement buildElement(PlaywrightElement element, Locator locator) {
        Class<? extends IElement> elementClass = element.getClass();
        try {
            return elementClass.getConstructor(String.class, Locator.class).newInstance(elementName, locator);
        } catch (Exception e) {
            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), element.getElementData().toString());
        }
    }

    private IElement buildElement(String elementName, Map<String, String> elementData) {
        String elementTypeName = elementData.get("Тип");
        Class<? extends IElement> elementClass = ElementType.getType(elementTypeName).getElementClass();
        return buildElement(elementName, elementClass, elementData);
    }
    private IElement buildElement(String elementName, Class<? extends IElement> elementClass, Map<String, String> elementData) {
        try {
            return elementClass.getConstructor(String.class, elementData.getClass())
                    .newInstance(elementName, elementData);
        } catch (Exception e) {
            throw new ElementNotBuiltException(e, elementName, elementClass.getName(), elementData.toString());
        }

    }

}
