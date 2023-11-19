package step.allure;

import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.testdata.TestData;

import java.util.LinkedHashMap;
import java.util.Map;

import static rs.taukit.utils.BasicUtils.scenarioData;

public class User {
    @Getter
    private TestData userData;

    public User(String userDescription) {
        LinkedHashMap<String, Object> userDataParameters = new LinkedHashMap<>();
        userDataParameters.put("тип", "тестовые данные");
        userDataParameters.put("файл", Config.getProperty("userFilePath"));
        userDataParameters.put("поиск по полям", Map.of("описание", userDescription));
        userData = new TestData(userDescription, userDataParameters);
        scenarioData().put(userDescription, userData);
    }

    public boolean equals(User other) {
        return getLogin().equalsIgnoreCase(other.getLogin());
    }

    public String getLogin() {
        return userData.get("логин");
    }
}
