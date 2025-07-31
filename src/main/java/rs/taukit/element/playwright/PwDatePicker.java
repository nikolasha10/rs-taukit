package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.Editable;
import rs.taukit.element.interfaces.ICalendar;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

import static rs.taukit.utils.BasicUtils.page;

public class PwDatePicker extends PwBlock implements ICalendar, Editable {
    private PwBlock activeRow;

    public PwDatePicker(String elementName, LinkedHashMap<String, Object> elementData) {
        this(elementName, new ElementModel(elementData));
    }

    public PwDatePicker(String elementName, IElement element) {
        this(elementName, element.getModel());
    }

    public PwDatePicker(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    public PwDatePicker(String elementName, ElementModel model) {
        super(elementName, model);
    }

    @Override
    public void pickDate(String date) {
        String[] chunks = date.split("\\.");
        String day = chunks[0];
        String month = chunks[1];
        String year = chunks[2];
        this.click();
        PwBlock calendarBlock = this.descendants.get("Календарь").castTo(PwBlock.class);
        calendarBlock.getElementByName("Год").click();
        PwBlock selectYear = calendarBlock.getElementByName("Выберите год").castTo(PwBlock.class);
        PwList yearList = selectYear.getElementByName("Выбор года").castTo(PwList.class);
        yearList.selectItem(year);

        calendarBlock.getElementByName("Месяц").click();
        PwBlock selectMonth = calendarBlock.getElementByName("Выберите месяц").castTo(PwBlock.class);
        PwList monthList = selectMonth.getElementByName("Выбор месяца").castTo(PwList.class);
        monthList.getItemList().get(Integer.parseInt(month) - 1).click();

        PwBlock calendar = calendarBlock.getElementByName("Календарь").castTo(PwBlock.class);
        PwList dayList = calendar.getElementByName("Выбор числа").castTo(PwList.class);
        if (dayList.contains(day)) {
            dayList.selectItem(day);
        } else {
            dayList.selectItem(day.replace("0", ""));
        }
    }

    @Override
    public void setValue(String value) {
        pickDate(value);
    }

    @Override
    public void setValue(String value, String controlValue) {
        String controlValueFinal = controlValue.equals("=") ? value : controlValue;
        driver.waitForCondition(() -> {
            setValue(value);
            page().waitForTimeout(1000);
            String currentValue = elementPwLocator.inputValue();
            if (!currentValue.equals(controlValueFinal)) {
                TauLogger.console("currentValue: %s, controlValue: %s", currentValue, controlValueFinal);
            }
            return currentValue.equals(controlValueFinal);
        });
    }
}
