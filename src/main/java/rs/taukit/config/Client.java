package rs.taukit.config;

import lombok.Getter;
import rs.taukit.exceptions.UserNotFoundException;
import rs.taukit.utils.YamlParser;

import java.util.List;
import java.util.Map;

@Getter
public class Client extends TestDataWork{
    private final String description;

    /**
     * Конструктор
     * @param description - описание клиента в файле с данными клиентов
     */
    public Client(String description) {
        this.description = description;
        try {
            String userFilePath = Config.getProperty("clientFilePath");
            List<Map<String, ?>> userList = YamlParser.parse(userFilePath);
            this.data = userList.stream().filter(this::hasDescription).findFirst().orElseThrow();
        } catch (Exception e){
            throw new UserNotFoundException(e, description);
        }
    }

    /**
     * Проверяет наличие описания клиента в карте <b>clientData</b>
     * @param clientData - карта с данными клиента
     * @return true, если поле <i>описание</i> содержит значение поля <b>description</b>. Иначе false.
     */
    private boolean hasDescription(Map<String,?> clientData){
        List<String> descriptionList = YamlParser.convert(clientData.get("описание"));
        return descriptionList.contains(description);
    }

    @Override
    public String toString() {
        return description;
    }
}
