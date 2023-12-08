package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.TimeoutError;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasDownloadableFile;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;

import java.io.File;
import java.util.LinkedHashMap;

public class FileDownload extends PwElement implements IHasDownloadableFile {

    public FileDownload(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public FileDownload(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public FileDownload(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator);
    }

    @Override
    public void click(String fileName) {
        try {
            driver.downloadByClick(this, fileName);
            String dirName = FileUtils.getDownloadFilePath();
            File file = new File(dirName, fileName);
            TauLogger.attachFile(fileName, "pdf", file.getAbsolutePath());
        } catch (TimeoutError e) {
            TauLogger.fail(e, "Не удалось дождаться окончания загрузки файлов");
        }

    }
}
