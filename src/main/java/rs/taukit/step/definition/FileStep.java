package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.FileManager;
import rs.taukit.testdata.TestDataFile;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.YamlParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.*;

public class FileStep extends FileManager {
    private final String downloadFilePath = FileUtils.getDownloadFilePath();

    @И("^нажать на \"(.+)\" и скачать файл \"(.+)\"$")
    public void downloadFile(String buttonName, String fileName) {
        super.init(downloadFilePath, eval(fileName));
        super.downloadFile(buttonName);
    }

    @И("^нажать на \"(.+)\" и скачать файлы:$")
    public void downloadFiles(String buttonName, DataTable dataTable) {
        super.init(downloadFilePath, eval(dataTable));
        super.downloadFiles(buttonName);
    }

    @И("^нажать на \"(.+)\" и выбрать загруженный файл \"(.+)\"$")
    public void attachDownloadedFile(String buttonName, String fileName) {
        super.init(downloadFilePath, fileName);
        super.attachDownloadedFile(buttonName, eval(fileName));
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
        super.init(testDataFile);
        super.attachFile(buttonName);
    }

    @И("^сохранить значение параметра \"(.+)\" в файл \"(.+)\"$")
    public void writeValueToFile(String parameterName, String fileName) {
        super.init(downloadFilePath, fileName);
        super.writeValueToFile(parameterName, eval(fileName));
    }

    @И("^сохранить значение параметра \"(.+)\" в файл:$")
    public void writeValueToFile(String parameterName, DataTable dataTable) {
        super.init(eval(dataTable));
        super.writeValueToFile(parameterName);
    }

    @И("^сохранить содержимое файла \"(.+)\", как \"(.+)\"$")
    public void saveFileContentAs(String testDataFileName, String parameterName) {
        TestDataFile testDataFile = scenarioData().get(testDataFileName);
        super.init(testDataFile);
        super.saveFileContentAs(testDataFileName, eval(parameterName));
    }

    @И("^служебный шаг: создать файл \"(.+)\" с параметрами:$")
    public void createFile(String parameterName, DataTable dataTable) {
        super.init(null, eval(dataTable));
        super.createFile(parameterName);
    }

    @И("^сохранить содержимое файла \"(.+)\" в файл \"(.+)\" с параметрами:$")
    public void executeScript(String sourceFileParameter, String destinationFileParameter, DataTable dataTable) {
        TestDataFile testDataFile = scenarioData().get(sourceFileParameter);
        super.init(testDataFile);
        super.saveFileWithParameters(destinationFileParameter, eval(dataTable));
    }

    @И("^выполнить команду \"(.+)\" и сохранить результат в файл \"(.+)\"$")
    public void executeScript(String command, String resultFileParameterName) {
        TestDataFile testDataFile = scenarioData().get(resultFileParameterName);
        super.init(testDataFile);
        super.execute(eval(command));
    }

    @И("^найти файл по маске \"(.+)\" в папке \"(.+)\" и сохранить, как \"(.+)\"$")
    public void findFileByMask(String format, String dirPath, String fileParameterName) {
        super.init(eval(dirPath), "_");
        super.findFileByMask(eval(format), fileParameterName);
    }

    @И("^переименовать файл \"(.+)\" в \"(.+)\"$")
    public void renameFile(String fileParameterName, String fileNewName) {
        TestDataFile testDataFile = scenarioData().get(fileParameterName);
        super.init(testDataFile);
        super.rename(eval(fileNewName));
    }

    @И("^проверить содержимое файла \"(.+)\" на соответствие эталону:$")
    public void validateFile(String fileParameterName, DataTable dataTable) {
        TestDataFile testDataFile = scenarioData().get(fileParameterName);
        super.init(testDataFile);
        super.validate(eval(dataTable));
    }

    @И("^проверить валидность файла \"(.+)\" по xsd-схеме \"(.+)\"$")
    public void validateXmlByXsd(String xmlFileName, String xsdFileName) {
        String fileDir = eval("${SCENARIO_TEMP_DIR}");
        super.init(fileDir, xmlFileName);
        super.validateByXsd(eval(xsdFileName));
    }

    @И("^проверить, что файл \"(.+)\" не пуст$")
    public void checkFileIsNotEmpty(String fileParameterName) {
        TestDataFile testDataFile = scenarioData().get(fileParameterName);
        super.init(testDataFile);
        super.checkFileIsNotEmpty(fileParameterName);
    }
}
