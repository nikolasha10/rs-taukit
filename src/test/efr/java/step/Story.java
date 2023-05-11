package step;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.page.PageObject;
import rs.taukit.testdata.TestData;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestDataUtils;
import rs.taukit.utils.YamlParser;
import step.utils.Utils;

import java.util.Map;

public class Story extends rs.taukit.step.BasicStory {
    @Step("войти в '{appName}'")
    public static void loginIntoApplication(String appName) {
        TestData user = testContext().getScenarioData().get("Пользователь");
        if (Utils.isNobodyLoggedIn() || Utils.needToChangeUser(user)) {
            Map<String, ?> startPageData = TestDataUtils.loadPageConfig("Вход в приложение");
            String relativeUrl = YamlParser.convert(startPageData.get("Адрес"));
            String url = Config.getSTAND().getUrl() + relativeUrl;
            scenarioData().put("Адрес стартовой страницы", url);
            Utils.subScenario.runScenario("Войти в ЕФР");
            testContext().setCurrentUser(user);
        }
    }

    @Step("найти клиента по серии '{series}' и номеру '{number}' паспорта")
    public static void findClientByPassport(String series, String number) {
        setValue("Серия", series);
        setValue("Номер", number);
        clickElement("Найти");
        waitForPageLoad("Продукты клиента");
    }

    @Step("{scenarioName}")
    public static void runScenario(String scenarioName) {
        Utils.runScenario(scenarioName);
    }
    @Step("{scenarioName}")
    public static void runScenario(String scenarioName, DataTable dataTable) {
        TauLogger.attachDataTable("Параметры сценария", dataTable);
        dataTable.asMaps().forEach(data -> {
            String parameterName = TestDataUtils.get(data,"Параметр");
            String parameterValue = TestDataUtils.get(data, "Значение");
            scenarioData().put(parameterName, parameterValue);
        });
        Utils.runScenario(scenarioName);
    }
}
