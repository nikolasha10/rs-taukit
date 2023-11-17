package rs.taukit.element.playwright;

import com.microsoft.playwright.TimeoutError;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.IHasDownloadableFile;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

public class FileDownload extends PwElement implements IHasDownloadableFile {

    public FileDownload(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }
    public FileDownload(String elementName, ElementModel model) {
        super(elementName, model);
    }

    @Override
    public void click() {
        try {
            driver.downloadByClick(this);
        } catch (TimeoutError e) {
            TauLogger.fail(e, "Не удалось дождаться окончания загрузки файлов");
        }

    }
}
