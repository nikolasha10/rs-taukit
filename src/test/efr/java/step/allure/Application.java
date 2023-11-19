package step.allure;

import rs.taukit.config.Config;
import rs.taukit.step.allure.BasicApplication;
import rs.taukit.testdata.TestData;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static rs.taukit.utils.BasicUtils.scenarioData;
import static rs.taukit.utils.BasicUtils.testContext;

public class Application extends BasicApplication {
    String appName;
    TestData appData;
    String startPageName;
    User currentUser;
    SubScenario subScenario = SubScenario.getInstance();

    public Application(String appName) {
        this.appName = appName;
        LinkedHashMap<String, Object> appDataParameters = new LinkedHashMap<>();
        appDataParameters.put("тип", "тестовые данные");
        appDataParameters.put("файл", Config.getProperty("applicationsFilePath"));
        appDataParameters.put("поиск по полям", Map.of("название", appName));
        this.appData = new TestData(appName, appDataParameters);
        this.startPageName = appData.get("стартовая страница");
    }

    public boolean isUserLoggedIn(User user) {
        return currentUser != null && currentUser.equals(user);
    }

    public void login(String userDescription) {
        User user = new User(userDescription);
        Application activeApp = (Application) testContext().getApplication(appName);
        Application app = Optional.ofNullable(activeApp).orElse(new Application(appName));
        if (!app.isUserLoggedIn(user)) {
            app.login(user);
            testContext().putApplication(appName, app);
        }
    }

    public void login(User user) {
        Map<String, ?> startPageData = TestDataUtils.loadPageConfig(startPageName);
        String relativeUrl = YamlParser.convert(startPageData.get("Адрес"));
        String url = Config.getSTAND().getUrl() + relativeUrl;
        scenarioData().put("Адрес стартовой страницы", url);
        scenarioData().put("Пользователь", user.getUserData());
        subScenario.runScenario(appData.get("сценарий входа"));
        currentUser = user;
    }
}
