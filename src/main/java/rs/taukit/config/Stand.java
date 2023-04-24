package rs.taukit.config;

import lombok.Getter;
import rs.taukit.exceptions.ConfigFileNotFoundException;
import rs.taukit.utils.YamlParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Getter
public class Stand {
    private final String name;
    private final String host;
    private final String port;

    private final String url;
    private final Map<String, ?> standData;
    public Stand(String standsConfigFilePath, String standName){
        name = standName;
        try {
            List<Map<String, ?>> standList = YamlParser.parse(standsConfigFilePath);
            standData = standList.stream().filter(this::isNameEquals).findFirst().orElseThrow();
            host = standData.get("hostApplication").toString();
            port = standData.get("portApplication").toString();
            url = String.format("%s://%s:%s", standData.get("protocol"), host, port);
        } catch (IOException e){
            throw new ConfigFileNotFoundException(e, standsConfigFilePath);
        }
    }

    private boolean isNameEquals(Map<String, ?> standMap){
        return standMap.get("Название").toString().equalsIgnoreCase(name);
    }

}
