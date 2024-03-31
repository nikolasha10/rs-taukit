package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicFileStep;
import rs.taukit.testdata.TestDataFile;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.YamlParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.*;

public class CucumberBasicFileStep {
    private AllureBasicFileStep fileStep(String fileDir, String fileName) {
        return new AllureBasicFileStep(fileDir, fileName);
    }

    private AllureBasicFileStep fileStep(String fileDir, DataTable dataTable) {
        return new AllureBasicFileStep(fileDir, dataTable);
    }

    private AllureBasicFileStep fileStep(TestDataFile testDataFile) {
        return new AllureBasicFileStep(testDataFile);
    }

    @И("^нажать на \"(.+)\" и скачать файл \"(.+)\"$")
    public void downloadFile(String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        fileStep(dirPath, eval(fileName)).downloadFile(buttonName);
    }

    @И("^нажать на \"(.+)\" и скачать файлы:$")
    public void downloadFiles(String buttonName, DataTable dataTable) {
        String dirPath = FileUtils.getDownloadFilePath();
        fileStep(dirPath, eval(dataTable)).downloadFiles(buttonName);
    }

    @И("^нажать на \"(.+)\" и выбрать загруженный файл \"(.+)\"$")
    public void attachDownloadedFile(String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        fileStep(dirPath, fileName).attachDownloadedFile(buttonName, eval(fileName));
    }

    @И("^нажать на \"(.+)\" и выбрать файл \"(.+)\"$")
    public void attachFile(String buttonName, String parameterName) {
        TestDataFile testDataFile;
        String format = "\\$\\{(.+?)} -> (.+)";
        Matcher matcher = Pattern.compile(format).matcher(parameterName);
        Matcher parameterValueMatcher = Pattern.compile(format).matcher(eval(parameterName));
        if (matcher.matches()) {
            testDataFile = evalObject(parameterName);
        } else if (parameterValueMatcher.matches()) {
            testDataFile = evalObject(eval(parameterName));
        } else {
            testDataFile = YamlParser.convert(scenarioData().get(eval(parameterName)));
        }
        fileStep(testDataFile).attachFile(buttonName);
    }

}
