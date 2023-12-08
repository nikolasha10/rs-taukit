package rs.taukit.step.allure;

import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasAttachment;
import rs.taukit.element.interfaces.IHasDownloadableFile;
import rs.taukit.utils.BasicUtils;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

public class AllureBasicFileStep extends BasicUtils {

    @Step("нажать на '{buttonName}' и скачать файл '{fileName}'")
    public void downloadFile(String buttonName, String fileName) {
        element(buttonName).castTo(IHasDownloadableFile.class).click(fileName);
    }

    @Step("нажать на '{buttonName}' и выбрать загруженный файл '{fileName}'")
    public void attachFile(String buttonName, String fileName) {
        String dirPath = FileUtils.getDownloadFilePath();
        element(buttonName).castTo(IHasAttachment.class).attach(dirPath, fileName);
        TauLogger.report("Нажата кнопка '%s'", buttonName);
        TauLogger.report("Выбран файл '%s' из папки '%s'", fileName, dirPath);
    }

}
