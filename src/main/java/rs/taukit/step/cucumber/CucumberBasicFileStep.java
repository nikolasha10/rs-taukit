package rs.taukit.step.cucumber;

import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicFileStep;
import rs.taukit.testdata.TestDataFile;
import rs.taukit.utils.FileUtils;

import static rs.taukit.utils.BasicUtils.eval;
import static rs.taukit.utils.BasicUtils.evalObject;

public class CucumberBasicFileStep {
    private AllureBasicFileStep fileStep(String fileDir, String fileName) {
        return new AllureBasicFileStep(fileDir, fileName);
    }
    private AllureBasicFileStep fileStep(TestDataFile testDataFile) {
        return new AllureBasicFileStep(testDataFile);
    }

    @И("^нажать на \"(.+)\" и скачать файл \"(.+)\"$")
    public void downloadFile(String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        fileStep(dirPath, fileName).downloadFile(buttonName, eval(fileName));
    }

    @И("^нажать на \"(.+)\" и выбрать загруженный файл \"(.+)\"$")
    public void attachDownloadedFile(String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        fileStep(dirPath, fileName).attachDownloadedFile(buttonName, eval(fileName));
    }

    @И("^нажать на \"(.+)\" и выбрать файл \"(.+)\"$")
    public void attachFile(String buttonName, String parameterName) {
        TestDataFile testDataFile = evalObject(parameterName);
        fileStep(testDataFile).attachFile(buttonName);
    }

}
