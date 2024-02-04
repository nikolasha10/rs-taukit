package step.cucumber;

import io.cucumber.java.ru.И;
import step.allure.AllureEfrCreditStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberEfrCreditStep {
    AllureEfrCreditStep creditStep() {
        return new AllureEfrCreditStep();
    }

    @И("^дождаться отображения действия \"(.+)\" на вкладке \"История\"$")
    public void loginIntoApplication(String actionName) {
        creditStep().waitForActionOnHistoryTab(eval(actionName));
    }

}
