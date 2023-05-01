package rs.taukit.testdata.constant;

import rs.taukit.exceptions.TestDataException;

import java.util.Arrays;
import java.util.Calendar;

public enum TDDateTime {
    MINUTE("минут(а|ы)?", Calendar.MINUTE),
    HOUR("час(а|ов)?", Calendar.HOUR),
    DAY("день|дн(я|ей)?", Calendar.DATE),
    MONTH("месяц(а|ев)?", Calendar.MONTH),
    YEAR("год(а)?|лет", Calendar.YEAR);
    private final String format;
    private final int calendarUnit;

    TDDateTime(String format, int calendarUnit) {
        this.format = format;
        this.calendarUnit = calendarUnit;
    }

    public static int unit(String units) {
        return Arrays.stream(values()).filter(value -> units.matches(value.format))
                .findFirst().orElseThrow(() -> new TestDataException(
                        "Не найдено соответствий для единиц измерения времени '%s'", units))
                .calendarUnit;
    }
}
