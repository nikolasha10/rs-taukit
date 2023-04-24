package rs.taukit.config;

import io.cucumber.datatable.DataTable;
import lombok.Getter;
import rs.taukit.exceptions.UserNotFoundException;
import rs.taukit.utils.YamlParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Employee extends TestDataWork{
    private final String description;
    private final String login;
    private final String password;

    /**
     * Конструктор
     * @param description - описание пользователя в файле с данными пользователей
     */
    public Employee(String description) {
        this.description = description;
        try {
            String userFilePath = Config.getProperty("userFilePath");
            List<Map<String, ?>> userList = YamlParser.parse(userFilePath);
            this.data = userList.stream().filter(this::hasDescription).findFirst().orElseThrow();
            this.login = get("логин");
            this.password = get("пароль");
        } catch (Exception e){
            throw new UserNotFoundException(e, description);
        }
    }

    /**
     * Проверяет наличие описания пользователя в карте <b>userData</b>
     * @param userData - карта с данными пользователя
     * @return true, если поле <i>описание</i> содержит значение поля <b>description</b>. Иначе false.
     */
    private boolean hasDescription(Map<String,?> userData){
        List<String> descriptionList = YamlParser.convert(userData.get("описание"));
        return descriptionList.contains(description);
    }

    @Override
    public String toString() {
        return description;
    }
}
