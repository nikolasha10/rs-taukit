package rs.taukit.testdata.constant;

import rs.taukit.exceptions.TestDataException;
import rs.taukit.testdata.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum TestDataField {
    TEST_DATA("тестовые данные", TestData.class),
    FILE("файл", TestDataFile.class),
    DATE("дата", TestDataDateTime.class),
    PDF_TEMPLATE_INSTRUCTION("инструкция шаблона PDF", TestDataInstruction.class);

    private static final Map<String, TestDataField> BY_NAME = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(value -> BY_NAME.put(value.typeName, value));
    }

    private final String typeName;
    private final Class<? extends ITestData> fieldClass;

    TestDataField(String typeName, Class<? extends ITestData> fieldClass) {
        this.typeName = typeName;
        this.fieldClass = fieldClass;
    }

    public static Class<? extends ITestData> getFieldClass(String name) {
        return BY_NAME.get(name).fieldClass;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ITestData> T getField(String typeName, String fieldName, Object constructorParameters) {
        try {
            return (T) getFieldClass(typeName).getConstructor(String.class, constructorParameters.getClass())
                    .newInstance(fieldName, constructorParameters);
        } catch (Exception e) {
            throw new TestDataException(e, "Не удалось создать объект тестовых данных \"%s\" с типом \"%s\"",
                    fieldName, typeName);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends ITestData> T getField(String typeName, String fieldName, Object constructorParameters, String dirPath) {
        try {
            return (T) getFieldClass(typeName).getConstructor(String.class, constructorParameters.getClass(), String.class)
                    .newInstance(fieldName, constructorParameters, dirPath);
        } catch (Exception e) {
            throw new TestDataException(e, "Не удалось создать объект тестовых данных \"%s\" с типом \"%s\"",
                    fieldName, typeName);
        }
    }

}
