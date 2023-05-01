package rs.taukit.testdata;

import lombok.Getter;
import rs.taukit.exceptions.TestDataException;
import rs.taukit.testdata.constant.TDSearchMethod;
import rs.taukit.utils.YamlParser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Getter
public class TestData implements ITestData {
    private DataStorage dataStorage;
    private String fieldName;
    private String searchValue;

    public TestData() {
    }

    public TestData(String fieldName, LinkedHashMap<String, ?> parameters) {
        this.fieldName = fieldName;
        String filePath = YamlParser.convert(parameters.get("файл"));
        String searchBy = YamlParser.convert(parameters.get("поиск по полю"));
        String searchMethodName = YamlParser.convert(parameters.get("метод поиска"));
        TDSearchMethod searchMethod = TDSearchMethod.getMethod(searchMethodName);
        String searchValue = YamlParser.convert(parameters.get(searchBy));
        this.searchValue = searchValue;
        List<Map<String, ?>> dataList = YamlParser.parse(filePath);
        Predicate<Map<String, ?>> condition = item -> searchMethod.search(item.get(searchBy), searchValue);
        Supplier<RuntimeException> e = () -> new TestDataException("Не удалось получить данные для поля '%s'", fieldName);
        dataStorage = new DataStorage(dataList.stream().filter(condition).findFirst().orElseThrow(e));
    }

    @Override
    public LinkedHashMap<String, Object> getAttachments() {
        return dataStorage.getAttachments(searchValue);
    }

    @Override
    public <T> T get(String fieldName) {
        return dataStorage.get(fieldName);
    }

    @Override
    public String getStringValue() {
        return searchValue;
    }
}
