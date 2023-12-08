package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicFileStep;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicFileStep {
    private AllureBasicFileStep fileStep() {
        return new AllureBasicFileStep();
    }

    @И("^нажать на \"(.+)\" и скачать файл \"(.+)\"$")
    public void downloadFile(String fileName, String buttonName) {
        fileStep().downloadFile(eval(fileName), buttonName);
    }

    @И("^нажать на \"(.+)\" и выбрать загруженный файл \"(.+)\"$")
    public void attachFile(String fileName, String buttonName) {
        fileStep().attachFile(eval(fileName), buttonName);
    }

}
