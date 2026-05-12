package rs.taukit.driver.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.options.Proxy;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.exceptions.FileNotCreatedException;
import rs.taukit.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static rs.taukit.utils.BasicUtils.scenarioData;

@Getter
public class Webkit extends PwBrowser {

    public Webkit(BrowserType browserType, int index, List<String> args) {
        super(browserType, index, args);
    }

    @Override
    protected Browser launch(BrowserType browserType) {
        File downloadDir = new File(Objects.requireNonNull(Config.getProperty("downloads.dir")));
        return browserType.launch(new BrowserType.LaunchOptions()
                .setHeadless(Config.getBooleanProperty("browser.headless"))
                .setChannel("chromium")
                .setDownloadsPath(downloadDir.toPath()));
    }

    @Override
    protected BrowserContext createContext(int index, List<String> args) {
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
        File downloadDir = new File(Objects.requireNonNull(Config.getProperty("downloads.dir")));
        int viewportWidth = Config.getIntegerProperty("window.width");
        int viewportHeight = Config.getIntegerProperty("window.height");
        BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
                .setViewportSize(viewportWidth, viewportHeight)
                .setHeadless(Config.getBooleanProperty("browser.headless"))
                .setChannel("chromium")
                .setDownloadsPath(downloadDir.toPath())
                .setIgnoreHTTPSErrors(true)
                .setArgs(args);
        String proxyServer = Config.getProperty("proxy.server");
        if (proxyServer != null) {
            Proxy proxy = new Proxy(proxyServer);
            options.setProxy(proxy);
        }
        Boolean videoSave = scenarioData().getOptional("video.save");
        if (Config.getBooleanProperty("video.save") || Optional.ofNullable(videoSave).orElse(false)) {
            options.setRecordVideoDir(Path.of(FileUtils.getDownloadFilePath(), "videos"));
            options.setRecordVideoSize(viewportWidth, viewportHeight);
        }
        return browser.browserType()
                .launchPersistentContext(
                        chromeDirExternally.getAbsoluteFile().toPath(),
                        options);
    }

    @Override
    protected void removeChromeDir(int index) {
        long threadId = Thread.currentThread().threadId();
        File chromeDirExternally = new File(String.format("target/chromium/%d_%d", threadId, index));
        FileUtils.deleteDirectory(chromeDirExternally.toPath());
    }
}
