package rs.taukit.step.allure;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasAttachment;
import rs.taukit.element.interfaces.IHasDownloadableFile;
import rs.taukit.testdata.TestDataFile;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

import java.io.File;

public class AllureBasicFileStep extends BasicUtils {
    private final String fileDir;
    private String fileName;
    private String fileExtension;
    private String filePath;
    private DataTable fileList;

    public AllureBasicFileStep(String fileDir, String fileName) {
        this.fileName = fileName;
        String[] chunks = fileName.split("\\.");
        this.fileExtension = chunks[chunks.length - 1];
        this.fileDir = fileDir;
        this.filePath = fileDir + File.separator + fileName;
    }

    public AllureBasicFileStep(String fileDir, DataTable dataTable) {
        this.fileDir = fileDir;
        this.fileList = dataTable;
    }

    public AllureBasicFileStep(TestDataFile testDataFile) {
        this.fileName = testDataFile.getFileName();
        this.fileExtension = testDataFile.getFileExtension();
        this.filePath = testDataFile.getFilePath();
        this.fileDir = filePath.replace(fileName, "");
    }

    @Step("нажать на '{buttonName}' и скачать файл '{this.fileName}'")
    public void downloadFile(String buttonName) {
        element(buttonName).castTo(IHasDownloadableFile.class).click(this.fileName);
    }

    @Step("нажать на '{buttonName}' и скачать файлы:")
    public void downloadFiles(String buttonName) {
        TauLogger.attachDataTable("Список загружаемых файлов", this.fileList);
        element(buttonName).castTo(IHasDownloadableFile.class).click(this.fileList);
    }

    @Step("нажать на '{buttonName}' и выбрать загруженный файл '{fileName}'")
    public void attachDownloadedFile(String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        element(buttonName).castTo(IHasAttachment.class).attach(dirPath, fileName);
        TauLogger.report("Нажата кнопка \"%s\"", buttonName);
        TauLogger.report("Выбран файл \"%s\" из папки \"%s\"", fileName, dirPath);
    }

    @Step("нажать на '{buttonName}' и выбрать файл '{this.fileName}' из папки '{this.fileDir}'")
    public void attachFile(String buttonName) {
        TauLogger.attachFile(fileName, filePath);
        element(buttonName).castTo(IHasAttachment.class).attach(filePath);
        TauLogger.report("Нажата кнопка \"%s\"", buttonName);
        TauLogger.report("Выбран файл \"%s\"", fileName);
    }

    @Step("сохранить значение параметра {parameterName} в файл {fileName}")
    public void writeValueToFile(String parameterName, String fileName) {
        FileUtils.writeFile(filePath, scenarioData().get(parameterName));
        TauLogger.report("Файл '%s' создан", filePath);
    }
}
