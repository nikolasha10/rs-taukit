package rs.taukit.utils;

import rs.taukit.exceptions.DataTableFormatMismatchException;

import java.util.Map;
import java.util.Optional;

public class TestDataUtils {
    public static <T> T get(Map<String,T> data, String fieldName){
        return Optional.ofNullable(data.get(fieldName))
                .orElseThrow(() -> new DataTableFormatMismatchException("Не найдено поле '%s'. Присутствуют поля: %s",
                        fieldName, data.keySet()));
    }
}
