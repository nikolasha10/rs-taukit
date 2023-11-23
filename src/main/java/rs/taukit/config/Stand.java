package rs.taukit.config;

import lombok.Getter;
import rs.taukit.utils.YamlParser;

import java.util.List;
import java.util.Map;

@Getter
public class Stand {
    private final String name;
    private final String host;
    private final String port;
    private final String dbSuite;

    private final String url;
    private final Map<String, ?> standData;

    public Stand(String standsConfigFilePath, String standName) {
        name = standName;
        List<Map<String, ?>> standList = YamlParser.parse(standsConfigFilePath);
        standData = standList.stream().filter(this::isNameEquals).findFirst().orElseThrow();
        host = standData.get("hostApplication").toString();
        port = standData.get("portApplication").toString();
        url = String.format("%s://%s:%s", standData.get("protocol"), host, port);
        dbSuite = standData.get("dbSuite").toString();
    }

    private boolean isNameEquals(Map<String, ?> standMap) {
        return standMap.get("Название").toString().equalsIgnoreCase(name);
    }

}
