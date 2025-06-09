package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.apache.commons.codec.digest.DigestUtils;
import org.xml.sax.SAXException;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.Downloadable;
import rs.taukit.element.interfaces.IHasAttachment;
import rs.taukit.testdata.TestDataFile;
import rs.taukit.utils.*;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rs.taukit.utils.BasicUtils.eval;
import static rs.taukit.utils.BasicUtils.scenarioData;

public class FileManager extends StepManager {
    private String fileDir;
    private String fileName;
    private String fileExtension;
    private String filePath;
    private TestDataFile testDataFile;
    private DataTable dataTable;

    public void init(String fileDir, String fileName) {
        this.fileDir = fileDir;
        this.fileName = fileName;
        String[] chunks = fileName.split("\\.");
        this.fileExtension = chunks[chunks.length - 1];
    }

    public void init(String fileDir, DataTable dataTable) {
        this.fileDir = fileDir;
        this.dataTable = dataTable;
    }

    public void init(TestDataFile testDataFile) {
        this.fileName = testDataFile.getFileName();
        this.fileExtension = testDataFile.getFileExtension();
        this.filePath = testDataFile.getFilePath();
        this.fileDir = filePath.replace(fileName, "");
    }

    public void init(DataTable dataTable) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        dataTable.asMaps().forEach(row -> dataMap.put(row.get("Параметр"), row.get("Значение")));
        this.fileName = TestDataUtils.get(dataMap, "название");
        this.fileDir = TestDataUtils.get(dataMap, "директория");
        LinkedHashMap<String, String> testDataMap = new LinkedHashMap<>();
        this.filePath = String.format("%s/%s", fileDir, fileName);
        testDataMap.put("путь", filePath);
        this.testDataFile = new TestDataFile(fileName, testDataMap);
        scenarioData.put(fileName, testDataFile);
    }

    private LinkedHashMap<String, String> getInfo(String stepName) {
        LinkedHashMap<String, String> info = new LinkedHashMap<>();
        info.put("Функционал", scenario.getFunctionality());
        info.put("Сценарий", scenario.getName());
        info.put("Шаг", stepName);
        return info;
    }

    @Step("нажать на '{buttonName}' и скачать файл '{this.fileName}'")
    public void downloadFile(String buttonName) {
        String stepName = String.format("%s -> нажать на '%s' и скачать файл '%s'",
                scenarioStorage.getStepStack(), buttonName, this.fileName);
        LinkedHashMap<String, String> info = getInfo(stepName);
        String hash = DigestUtils.md5Hex(info.toString());
        String basePath = Config.getProperty("downloads.dir") + File.separator + hash;
        String infoPath = basePath + "-info.yaml";
        String filePath = basePath + "-file." + fileExtension;
        File file = new File(filePath);
        run(() -> {
            Downloadable downloadable = getElement(buttonName);
            downloadable.click(file);
        }, "Не удалось нажать на '%s' и скачать файл '%s'", buttonName, fileName);
        FileUtils.writeFile(Path.of(infoPath), YamlParser.write(info, false));
        TauLogger.attachFile(this.fileName, file.getAbsolutePath());
        LinkedHashMap<String, String> fileData = new LinkedHashMap<>();
        fileData.put("тип", "файл");
        fileData.put("путь", file.getPath());
        this.testDataFile = new TestDataFile(this.fileName, fileData);
        scenarioData().put(this.fileName, this.testDataFile);
    }

    @Step("нажать на '{buttonName}' и скачать файлы:")
    public void downloadFiles(String buttonName) {
        TauLogger.attachDataTable("Список загружаемых файлов", this.dataTable);
        Downloadable downloadable = getElement(buttonName);
        run(() -> downloadable.click(this.dataTable),
                "Не удалось нажать на '%s' и скачать указанные файлы", buttonName);
    }

    @Step("нажать на '{buttonName}' и выбрать загруженный файл '{fileName}'")
    public void attachDownloadedFile(String buttonName, String fileName) {
        IHasAttachment attachment = getElement(buttonName);
        run(() -> attachment.attach(this.testDataFile.getFilePath()),
                "Не удалось нажать на кнопку '%s' и выбрать файл '%s'", buttonName, fileName);
        TauLogger.console("Нажата кнопка '%s'", buttonName);
        TauLogger.console("Выбран файл '%s' из папки '%s'", fileName, fileDir);
    }

    @Step("нажать на '{buttonName}' и выбрать файл '{this.fileName}' из папки '{this.fileDir}'")
    public void attachFile(String buttonName) {
        TauLogger.attachFile(fileName, filePath);
        IHasAttachment attachment = getElement(buttonName);
        run(() -> attachment.attach(filePath),
                "Не удалось нажать на кнопку '%s' и выбрать файл '%s' из папки '%s'",
                buttonName, fileName, fileDir);
        TauLogger.console("Нажата кнопка '%s'", buttonName);
        TauLogger.console("Выбран файл '%s'", fileName);
    }

    @Step("сохранить значение параметра {parameterName} в файл {fileName}")
    public void writeValueToFile(String parameterName, String fileName) {
        run(() -> FileUtils.writeFile(filePath, scenarioData.get(parameterName).toString()),
                "Не удалось сохранить значение параметра '%s' в файл '%s'", parameterName, fileName);
        TauLogger.console("Файл '%s' создан", filePath);
    }

    @Step("сохранить значение параметра {parameterName} в файл:")
    public void writeValueToFile(String parameterName) {
        run(() -> FileUtils.writeFile(filePath, scenarioData.get(parameterName).toString()),
                "Не удалось сохранить значение параметра '%s' в файл", parameterName);
        TauLogger.console("Файл '%s' создан", filePath);
    }

    @Step("сохранить содержимое файла '{fileName}', как '{parameterName}'")
    public void saveFileContentAs(String fileName, String parameterName) {
        scenarioData.put(parameterName, eval(FileUtils.readFile(filePath)));
        TauLogger.report("Параметр '%s' содержит значение '%s'", parameterName, scenarioData.get(parameterName));
    }

    @Step("выполнить команду '{command}' и сохранить результат в файл '{this.fileName}'")
    public void execute(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        File file = new File(this.filePath);
        processBuilder.redirectOutput(file);
        TauLogger.console("Выполняется команда: %s", command);
        try {
            Process process = processBuilder.start();
            while (process.isAlive()) ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TauLogger.report("Команда '%s' выполнена", command);
    }

    public void createFile(String testFileParameterName) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        dataTable.asMaps().forEach(row -> dataMap.put(row.get("Параметр"), row.get("Значение")));
        this.fileName = TestDataUtils.get(dataMap, "название");
        this.fileDir = TestDataUtils.get(dataMap, "директория");
        File file = new File(this.fileDir, this.fileName);
        this.filePath = file.getAbsolutePath();
        FileUtils.createFile(this.filePath);
        Map<String, String> testFileMap = new LinkedHashMap<>();
        testFileMap.put("тип", "файл");
        testFileMap.put("путь", this.filePath);
        TestDataFile testDataFile = new TestDataFile(testFileParameterName, YamlParser.convert(testFileMap));
        scenarioData.put(testFileParameterName, testDataFile);
    }

    @Step("сохранить содержимое файла '{this.fileName}' в файл '{destinationFileParameter}' с параметрами:")
    public void saveFileWithParameters(String destinationFileParameter, DataTable dataTable) {
        TauLogger.attachDataTable("Подстановка значений", dataTable);
        scenarioData.put(dataTable);
        TestDataFile testDataFile = scenarioData.get(destinationFileParameter);
        Map<String, String> dataMap = new HashMap<>();
        dataTable.asMaps().forEach(map -> dataMap.put(map.get("Параметр"), map.get("Значение")));
        String sourceEncoding = TestDataUtils.getOptional(dataMap, "кодировка источника");
        String resultEncoding = TestDataUtils.getOptional(dataMap, "кодировка результата");
        String content = FileUtils.readFile(filePath, sourceEncoding);
        FileUtils.writeFile(testDataFile.getFilePath(), eval(content), resultEncoding);
        TauLogger.report("Содержимое файла '%s' сохранено в файл '%s'", this.fileName, testDataFile.getFileName());
    }

    @Step("найти файл по маске '{format}' в папке '{this.fileDir}' и сохранить, как '{fileParameterName}'")
    public void findFileByMask(String format, String fileParameterName) {
        File directory = new File(fileDir);
        FilenameFilter filenameFilter = (dir, name) -> {
            Matcher matcher = Pattern.compile(format).matcher(name);
            return matcher.matches();
        };
        String[] found = directory.list(filenameFilter);
        if (found != null && found.length > 0) {
            LinkedHashMap<String, String> fileMap = new LinkedHashMap<>();
            fileMap.put("тип", "файл");
            fileMap.put("путь", fileDir + "/" + found[0]);
            TestDataFile testDataFile = new TestDataFile(fileParameterName, YamlParser.convert(fileMap));
            scenarioData.put(fileParameterName, testDataFile);
            TauLogger.report("Файл '%s' был сохранён в параметр '%s'", testDataFile.getFileName(), fileParameterName);
        } else {
            TauLogger.fail("Не удалось найти файл в папке '%s', соответствующий маске '%s'", fileDir, format);
        }
    }

    @Step("переименовать файл '{this.fileName}' в '{fileNewName}'")
    public void rename(String fileNewName) {
        File file = new File(filePath);
        File newFile = new File(fileDir, fileNewName);
        int i = 1;
        while (newFile.exists()) {
            String tempName = String.format("%s_%d", fileNewName, i++);
            newFile = new File(tempName);
        }
        if (file.renameTo(newFile)) {
            TauLogger.report("Файл '%s' переименован в '%s'", fileName, newFile.getName());
        } else {
            TauLogger.error("Не удалось переименовать файл '%s' в '%s'", fileName, newFile.getName());
        }
    }

    @Step("проверить содержимое файла '{this.fileName}' на соответствие эталону")
    public void validate(DataTable dataTable) {
        String nameSpace = "Проверка содержимого файла";
        scenarioData.put(dataTable, nameSpace);
        String standardFileRelativePath = scenarioData.get(nameSpace, "эталон");
        String standardFilePath = FileUtils.getTestFilePath(standardFileRelativePath);
        File standardFile = new File(standardFilePath);
        if (!standardFile.exists()) {
            throw new RuntimeException(String.format("Не удалось найти файл %s", standardFilePath));
        }
        String testContent = FileUtils.readFile(filePath);
        String standardContent = eval(FileUtils.readFile(standardFilePath));
        String tempFilePath = String.format("%s/%s", eval("${SCENARIO_TEMP_DIR}"), standardFile.getName());
        FileUtils.writeFile(tempFilePath, standardContent);
        TauLogger.attachFile(fileName, filePath);
        TauLogger.attachFile(standardFile.getName(), tempFilePath);

        if (!testContent.equals(standardContent)) {
            scenarioData.put("testName", scenario.getFunctionality() + " " + scenario.getName());
            scenarioData.put("pageNumber", "1");
            scenarioData.put("ignoredZonesData", "");
            BufferedImage testTextImage = ImageUtils.textToImage(testContent, standardContent);
            BufferedImage standardTextImage = ImageUtils.textToImage(standardContent, testContent);
            BufferedImage differencesImage = ImageUtils.getDifferencesImage(testTextImage, standardTextImage);
            if (differencesImage != null) {
                String attachment = ImageUtils.getCompareResult(
                        testTextImage,
                        standardTextImage,
                        differencesImage);
                Runnable runnable = () -> TauLogger.attachFile("Различия содержимого файлов", attachment.getBytes());
                TauLogger.fail(runnable, "Содержимое файла '%s' не соответствует ожидаемому", fileName);
            }
        }
        TauLogger.console("Содержимое файла '%s' соответствует эталону", fileName);
    }

    @Step("проверить валидность файла '{this.fileName}' по xsd-схеме '{xsdFileName}'")
    public void validateByXsd(String xsdFileName) {
        String xsdFilesDir = Config.getProperty("xsd.files.dir");
        File schemaFile = new File(xsdFilesDir, xsdFileName);
        TauLogger.attachFile(fileName, filePath);
        TauLogger.attachFile(xsdFileName, schemaFile.getPath());
        Source xmlFile = new StreamSource(new File(filePath));
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
        } catch (SAXException e) {
            TauLogger.fail(e, "Файл '%s' не прошёл валидацию по схеме '%s'", fileName, xsdFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TauLogger.report("Файл валиден");
    }
}
