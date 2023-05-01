package rs.taukit.testdata.constant;

import rs.taukit.utils.YamlParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TDSearchMethod {
    SEARCH_IN_LIST ("значение в списке"){
        @Override
        public boolean search (Object object, String value){
            List<String> list = YamlParser.convert(object);
            return list.contains(value);
        }
    };

    private static final Map<String, TDSearchMethod> BY_METHOD_NAME = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(value -> BY_METHOD_NAME.put(value.methodName, value));
    }

    private final String methodName;

    TDSearchMethod(String methodName) {
        this.methodName = methodName;
    }

    public static TDSearchMethod getMethod(String methodName){
        return BY_METHOD_NAME.get(methodName);
    }

    public abstract boolean search(Object object, String value);
}
