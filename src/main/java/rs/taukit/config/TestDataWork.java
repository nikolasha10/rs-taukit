package rs.taukit.config;

import io.cucumber.datatable.DataTable;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TextUtils;
import rs.taukit.utils.YamlParser;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TestDataWork implements ITestData{
    protected Map<String, ?> data;
    protected Map<String, TestDataWork> workData;
    public Map<String, DataTable> getDataTables() {
        Map<String, DataTable> linkTableMap = new HashMap<>();
        List<List<String>> currentData = new ArrayList<>();
        data.forEach((fieldName, fieldValue) -> {
            if (fieldValue instanceof Map){
                Map<String, ?> value = YamlParser.convert(fieldValue);
                String className = YamlParser.convert(value.get("класс"));
                List<String> parameters = YamlParser.convert(value.get("параметры"));
                Class<?>[] pt = new Class<?>[parameters.size()];
                Arrays.fill(pt, String.class);
                try {
                    Class<?> dataClass = ClassLoader.getSystemClassLoader().loadClass(className);
                    TestDataWork testData = (TestDataWork) dataClass.getConstructor(pt).newInstance(parameters.toArray());
                    linkTableMap.putAll(testData.getDataTables());
                    currentData.add(Arrays.asList(fieldName, testData.toString()));
                    data.replace(fieldName,YamlParser.convert(testData));
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else if (fieldValue instanceof String) {
                String value = evalStrings(YamlParser.convert(fieldValue));
                currentData.add(Arrays.asList(fieldName, value));
                data.replace(fieldName,YamlParser.convert(value));
            } else if (fieldValue instanceof Integer){
                currentData.add(Arrays.asList(fieldName, YamlParser.convert(fieldValue)));
            }
        });
        Map<String, DataTable> result = new HashMap<>();
        result.put(toString(), DataTable.create(currentData, TextUtils.tableConverter));
        result.putAll(linkTableMap);
        return result;
    }

    public String evalStrings(String template){
        String format = "\\$\\{(.+?)}";
        Matcher matcher = Pattern.compile(format).matcher(template);
        return matcher.replaceAll(matchResult -> get(matchResult.group(1)));
    }

    public String evalObjects(String template){
        String format = "\\$\\{(.+?)} -> (.+)";
        Matcher matcher = Pattern.compile(format).matcher(template);
        return matcher.replaceFirst(matchResult -> {
            try {
                TestDataWork workData = get(matchResult.group(1));
                return workData.get(matchResult.group(2));
            } catch (ClassCastException ignored){
                return "null";
            }
        });

    }

    @SuppressWarnings("unchecked")
    public  <T> T get(String fieldName){
        if (data.containsKey(fieldName))
            return YamlParser.convert(data.get(fieldName));
        else return (T) String.format("${%s}",fieldName);
    }

}
