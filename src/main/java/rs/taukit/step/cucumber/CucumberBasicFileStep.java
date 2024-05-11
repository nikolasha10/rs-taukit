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

    @И("^сохранить значение параметра \"(.+)\" в файл \"(.+)\"$")
    public void writeValueToFile(String parameterName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        fileStep(dirPath, fileName).writeValueToFile(parameterName, eval(fileName));
    }

    @И("^сохранить содержимое файла \"(.+)\", как \"(.+)\"$")
    public void saveFileContentAs(String testDataFileName, String parameterName) {
        TestDataFile testDataFile = scenarioData().get(testDataFileName);
        fileStep(testDataFile).saveFileContentAs(testDataFileName, parameterName);
    }

    @И("^служебный шаг: создать файл \"(.+)\" с параметрами:$")
    public void createFile(String parameterName, DataTable dataTable) {
        DataTable newTable = eval(dataTable);
        fileStep(null, newTable).createFile(parameterName);
    }

    @И("^сохранить содержимое файла \"(.+)\" в файл \"(.+)\" с параметрами:$")
    public void executeScript(String sourceFileParameter, String destinationFileParameter, DataTable dataTable) {
        TestDataFile testDataFile = scenarioData().get(sourceFileParameter);
        fileStep(testDataFile).saveFileWithParameters(destinationFileParameter, eval(dataTable));
    }

    @И("^выполнить команду \"(.+)\" и сохранить результат в файл \"(.+)\"$")
    public void executeScript(String command, String resultFileParameterName) {
        TestDataFile testDataFile = scenarioData().get(resultFileParameterName);
        fileStep(testDataFile).execute(eval(command));
    }

    @И("^найти файл по маске \"(.+)\" в папке \"(.+)\" и сохранить, как \"(.+)\"$")
    public void findFileByMask(String format, String dirPath, String fileParameterName) {
        fileStep(eval(dirPath), "_").findFileByMask(eval(format), fileParameterName);
    }

    @И("^переименовать файл \"(.+)\" в \"(.+)\"$")
    public void renameFile(String fileParameterName, String fileNewName) {
        TestDataFile testDataFile = scenarioData().get(fileParameterName);
        fileStep(testDataFile).rename(eval(fileNewName));
    }

}
