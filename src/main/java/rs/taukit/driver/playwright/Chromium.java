package rs.taukit.driver.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.exceptions.FileNotCreatedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Getter
public class Chromium extends PwBrowser {

    public Chromium(BrowserType browserType, int index, boolean openPdf) {
        super(browserType, index, openPdf);
    }

    @Override
    protected Browser launch(BrowserType browserType) {
        File downloadDir = new File(Config.getProperty("downloads.dir"));
        return browserType.launch(new BrowserType.LaunchOptions()
                .setHeadless(Config.getBooleanProperty("browser.headless"))
                .setDownloadsPath(downloadDir.toPath()));
    }

    @Override
    protected BrowserContext createContext(int index, boolean openPdf) {
        long threadId = Thread.currentThread().threadId();
        File chromeDirExternally = new File(String.format("target/chromium/%d_%d/open_pdf_externally", threadId, index));
        File defaultDirExternally = new File(chromeDirExternally.getPath(), "Default");
        File preferencesExternally = new File(defaultDirExternally.getPath(), "Preferences");
        if (!preferencesExternally.exists()) {
            try {
                Files.createDirectories(defaultDirExternally.toPath());
                Files.writeString(
                        preferencesExternally.toPath(),
                        "{\"plugins\":{\"always_open_pdf_externally\":true}}",
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new FileNotCreatedException(e, preferencesExternally.getPath());
            }
        }
        File chromeDirInternally = new File(String.format("target/chromium/%d_%d/open_pdf_internally", threadId, index));
        File defaultDirInternally = new File(chromeDirInternally.getPath(), "Default");
        File preferencesInternally = new File(defaultDirInternally.getPath(), "Preferences");
        if (!preferencesInternally.exists()) {
            try {
                Files.createDirectories(defaultDirInternally.toPath());
                Files.writeString(
                        preferencesInternally.toPath(),
                        "{\"plugins\":{\"always_open_pdf_externally\":false}}",
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new FileNotCreatedException(e, preferencesExternally.getPath());
            }
        }
        File persistentContext = openPdf ? chromeDirInternally : chromeDirExternally;
        File downloadDir = new File(Config.getProperty("downloads.dir"));
        return browser.browserType()
                .launchPersistentContext(
                        persistentContext.getAbsoluteFile().toPath(),
                        new BrowserType.LaunchPersistentContextOptions()
                                .setViewportSize(
                                        Config.getIntegerProperty("window.width"),
                                        Config.getIntegerProperty("window.height"))
                                .setHeadless(Config.getBooleanProperty("browser.headless"))
                                .setDownloadsPath(downloadDir.toPath())
                                .setIgnoreHTTPSErrors(true));
    }
}
