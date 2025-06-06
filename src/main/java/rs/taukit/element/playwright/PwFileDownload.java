package rs.taukit.element.playwright;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.TimeoutError;
import io.cucumber.datatable.DataTable;
import rs.taukit.config.Config;
import rs.taukit.element.ElementModel;
import rs.taukit.element.interfaces.Downloadable;
import rs.taukit.utils.TauLogger;

import java.io.File;
import java.util.LinkedHashMap;

import static rs.taukit.utils.BasicUtils.scenario;

public class PwFileDownload extends PwElement implements Downloadable {

    public PwFileDownload(String elementName, LinkedHashMap<String, Object> elementData) {
        super(elementName, elementData);
    }

    public PwFileDownload(String elementName, ElementModel model) {
        super(elementName, model);
    }

    public PwFileDownload(String elementName, Locator locator, ElementModel model) {
        super(elementName, locator, model);
    }

    @Override
    public void click(File file) {
        try {
            driver.getCurrentBrowser().downloadByClick(this, file);
        } catch (TimeoutError e) {
            TauLogger.fail(e, "Не удалось дождаться окончания загрузки файлов");
        }

    }

    @Override
    public void click(DataTable dataTable) {
        driver.getCurrentBrowser().downloadByClick(this, dataTable);
    }

    @Override
    public boolean isDownloaded(String fileName) {
        String dirName = String.format("%s/%s", Config.getProperty("downloads.dir"), scenario().getName());
        return new File(dirName, fileName).exists();
    }
}
