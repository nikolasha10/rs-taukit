package rs.taukit.testdata;

import lombok.Getter;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.testdata.constant.TDDateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDataDateTime implements ITestData {
    private final String format;
    private final String formula;
    @Getter
    private final Calendar calendar;
    private final DataStorage dataStorage;
    private String fieldName;

    public TestDataDateTime(String fieldName, LinkedHashMap<String, ?> data) {
        this(fieldName, data, "");
    }

    public TestDataDateTime(String fieldName, LinkedHashMap<String, ?> data, String dirPath) {
        this.fieldName = fieldName;
        this.dataStorage = new DataStorage(data);
        this.format = get("формат");
        this.formula = get("формула");
        String dateNowFormat = "сегодня|сейчас";
        String specifiedDateFormat = "\\d{2}\\.\\d{2}\\.\\d{4}";
        String dateFormat = String.format("%s|%s", dateNowFormat, specifiedDateFormat);
        String shortTimeFormat = "\\d{2}:\\d{2}";
        String accurateTimeFormat = "\\d{2}:\\d{2}:\\d{2}";
        String timeFormat = String.format("(?:\\s%s(?::\\d{2})?)?", shortTimeFormat);
        String basicDateTimeFormat = String.format("(%s%s)", dateFormat, timeFormat);
        String modifierFormat = "(плюс|минус)";
        String amountFormat = "(\\d+)";
        String unitFormat = "(минут(?:а|ы)?|час(?:а|ов)?|день|дн(?:я|ей)?|месяц(?:а|ев)?|год(?:а)?|лет)";
        String format = String.format("%s(?:\s%s\s%s\s%s)?", basicDateTimeFormat, modifierFormat, amountFormat, unitFormat);
        Matcher matcher = Pattern.compile(format).matcher(formula);
        if (!matcher.matches()) {
            throw new TestDataException("Неправильный формат формулы для расчета значения поля \"%s\"", fieldName);
        }
        this.calendar = new GregorianCalendar();
        String dateParseFormat = "";
        String basicDateExpr = matcher.group(1);
        if (basicDateExpr.matches(accurateTimeFormat)) {
            dateParseFormat = "dd.MM.yyyy HH:mm:ss";
        } else if (basicDateExpr.matches(shortTimeFormat)) {
            dateParseFormat = "dd.MM.yyyy HH:mm";
        } else if (basicDateExpr.matches(specifiedDateFormat)) {
            dateParseFormat = "dd.MM.yyyy";
        }
        if (!dateParseFormat.isEmpty()) {
            DateFormat df = new SimpleDateFormat(dateParseFormat, Locale.forLanguageTag("ru"));
            try {
                calendar.setTime(df.parse(basicDateExpr));
            } catch (ParseException e) {
                throw new TestDataException(e, "Не удалось вычислить значение поля \"%s\" по формуле \"%s\"", fieldName, formula);
            }
        }
        String modifier = matcher.group(2);
        if (modifier != null) {
            String stringAmount = matcher.group(3);
            String units = matcher.group(4);
            int amount = Integer.parseInt(stringAmount);
            if (modifier.equals("минус")) amount = -amount;
            calendar.add(TDDateTime.unit(units), amount);
        }
    }

    @Override
    public LinkedHashMap<String, Object> getAttachments() {
        return dataStorage.getAttachments(toString());
    }

    @Override
    public <T> T get(String fieldName) {
        return dataStorage.get(fieldName);
    }

    @Override
    public <T> T getOptional(String fieldName) {
        return dataStorage.getOptional(fieldName);
    }

    @Override
    public String getStringValue() {
        LocalDate localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE));
        LocalTime localTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        return DateTimeFormatter.ofPattern(format).format(LocalDateTime.of(localDate, localTime));
    }
}
