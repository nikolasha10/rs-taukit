package rs.taukit.testdata;

import io.cucumber.datatable.DataTable;
import lombok.Getter;
import rs.taukit.utils.TextUtils;
import rs.taukit.utils.YamlParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
public class TestDataInstruction implements ITestData {
    private final String fieldName;
    private final DataStorage dataStorage;
    private final LinkedHashMap<String, List<String>> content;

    public TestDataInstruction(String fieldName, LinkedHashMap<String, ?> data) {
        this.fieldName = fieldName;
        this.dataStorage = new DataStorage(data);
        this.content = YamlParser.parse(dataStorage.get("файл"));
    }

    @Override
    public LinkedHashMap<String, Object> getAttachments() {
        LinkedHashMap<String, Object> attachments = new LinkedHashMap<>();
        attachments.put(fieldName, getDataTable());
        return attachments;
    }

    @Override
    public <T> T get(String fieldName) {
        return dataStorage.get(fieldName);
    }

    @Override
    public String getStringValue() {
        return fieldName;
    }

    public DataTable getDataTable() {
        List<List<String>> dataList = new ArrayList<>();
        dataList.add(Arrays.asList("Директория", "Фрагмент"));
        content.forEach((folder, fragmentList) -> fragmentList
                .forEach(fragment -> dataList.add(Arrays.asList(folder, fragment))));
        return DataTable.create(dataList, TextUtils.tableConverter);
    }
}
