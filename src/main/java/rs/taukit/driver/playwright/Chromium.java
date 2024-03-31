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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Chromium extends PwBrowser {

    public Chromium(BrowserType browserType, int index, boolean openPdf) {
        super(browserType, index, openPdf);
    }

    @Override
    protected Browser launch(BrowserType browserType) {
        String browserExtensionPath = new File(Config.getProperty("browser.extensions") + "/chromium").getAbsolutePath();
        List<String> args = Arrays.asList(
                "--load-extension=" + browserExtensionPath,
                "--disable-extensions-except=" + browserExtensionPath,
                "--embedded-extension-options=true");
        File downloadDir = new File(Config.getProperty("downloads.dir"));
        return browserType.launch(new BrowserType.LaunchOptions()
                .setArgs(args)
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
                Files.writeString(preferencesExternally.toPath(),
                        "{\"plugins\":{\"always_open_pdf_externally\":true}}", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
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
                Files.writeString(preferencesInternally.toPath(),
                        "{\"plugins\":{\"always_open_pdf_externally\":false}}", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new FileNotCreatedException(e, preferencesExternally.getPath());
            }
        }

        List<String> args = new ArrayList<>();
        File persistentContext = chromeDirExternally;
        if (openPdf) {
            String browserExtensionPath = new File("browserExtensions/chromium").getAbsolutePath();
            args.addAll(Arrays.asList(
                    "--load-extension=" + browserExtensionPath,
                    "--disable-extensions-except=" + browserExtensionPath,
                    "--embedded-extension-options=true"));
            persistentContext = chromeDirInternally;
        }
        File downloadDir = new File(Config.getProperty("downloads.dir"));
        return browser.browserType()
                .launchPersistentContext(
                        persistentContext.getAbsoluteFile().toPath(),
                        new BrowserType.LaunchPersistentContextOptions()
                                .setArgs(args)
                                .setViewportSize(
                                        Config.getIntegerProperty("window.width"),
                                        Config.getIntegerProperty("window.height"))
                                .setHeadless(Config.getBooleanProperty("browser.headless"))
                                .setDownloadsPath(downloadDir.toPath())
                                .setIgnoreHTTPSErrors(true));
    }
}
