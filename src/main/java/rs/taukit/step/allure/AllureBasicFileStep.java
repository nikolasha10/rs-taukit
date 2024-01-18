package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasAttachment;
import rs.taukit.element.interfaces.IHasDownloadableFile;
import rs.taukit.testdata.TestDataFile;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

import java.io.File;

public class AllureBasicFileStep extends BasicUtils {
    private final String fileName;
    private final String fileExtension;
    private final String filePath;
    private final String fileDir;

    public AllureBasicFileStep(String fileDir, String fileName) {
        this.fileName = fileName;
        String[] chunks = fileName.split("\\.");
        this.fileExtension = chunks[chunks.length - 1];
        this.fileDir = fileDir;
        this.filePath = fileDir + File.separator + fileName;
    }

    public AllureBasicFileStep(TestDataFile testDataFile) {
        this.fileName = testDataFile.getFileName();
        this.fileExtension = testDataFile.getFileExtension();
        this.filePath = testDataFile.getFilePath();
        this.fileDir = filePath.replace(fileName, "");
    }

    @Step("нажать на '{buttonName}' и скачать файл '{fileName}'")
    public void downloadFile(String buttonName, String fileName) {
        element(buttonName).castTo(IHasDownloadableFile.class).click(fileName);
    }

    @Step("нажать на '{buttonName}' и выбрать загруженный файл '{fileName}'")
    public void attachDownloadedFile(String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        element(buttonName).castTo(IHasAttachment.class).attach(dirPath, fileName);
        TauLogger.report("Нажата кнопка '%s'", buttonName);
        TauLogger.report("Выбран файл '%s' из папки '%s'", fileName, dirPath);
    }

    @Step("нажать на '{buttonName}' и выбрать файл '{this.fileName}' из папки '{this.fileDir}'")
    public void attachFile(String buttonName) {
        TauLogger.attachFile(fileName, fileExtension, filePath);
        element(buttonName).castTo(IHasAttachment.class).attach(filePath);
        TauLogger.report("Нажата кнопка '%s'", buttonName);
        TauLogger.report("Выбран файл '%s'", fileName);
    }
}
