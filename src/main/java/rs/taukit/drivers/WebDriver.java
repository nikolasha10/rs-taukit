package rs.taukit.drivers;

public interface WebDriver {
    void openBrowser(String browserName);

    void reopenBrowser();

    void close();
}
