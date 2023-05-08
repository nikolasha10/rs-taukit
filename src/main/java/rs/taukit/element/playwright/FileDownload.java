package rs.taukit.element.playwright;

import com.microsoft.playwright.TimeoutError;
import io.qameta.allure.Step;
import rs.taukit.element.interfaces.IHasDownloadableFile;
import rs.taukit.utils.TauLogger;

import java.util.LinkedHashMap;

public class FileDownload extends PlaywrightElement implements IHasDownloadableFile {

    public FileDownload(String elementName, LinkedHashMap<String, String> elementData) {
        super(elementName,elementData);
    }

    @Override
    public void click() {
        try {
            driver.downloadByClick(this);
        } catch (TimeoutError e){
            TauLogger.fail(e, "Не удалось дождаться окончания загрузки файлов");
        }

    }
}
