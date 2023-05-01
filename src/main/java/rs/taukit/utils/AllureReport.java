package rs.taukit.utils;

import com.google.gson.Gson;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.exceptions.ConfigFileNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Getter
public class AllureReport {
    private static List<File> getFileList(String scenarioName, long testEndTime) {
        File directory = new File(Config.getProperty("allure-results.path"));
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(file -> file.getName().endsWith("result.json")).
                filter(file -> (file.lastModified() >= testEndTime))
                .filter(file -> {
                    try {
                        String fileContent = Files.readString(file.toPath());
                        return fileContent.contains(scenarioName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    public static synchronized void editReports(String scenarioName, String functionality, long testEndTime) {
        getFileList(scenarioName, testEndTime).forEach(file -> replaceReport(file, scenarioName, functionality));
    }

    @SuppressWarnings("unchecked")
    private static void replaceReport(File file, String scenarioName, String functionality) {
        try {
            String fileContent = Files.readString(file.toPath());
            Gson gson = new Gson();
            Map<Object, Object> report = gson.fromJson(fileContent, Map.class);
            report.replace("name", scenarioName);
            List<Map<String, String>> labels = new ArrayList<>();
            Map<String, String> suiteName = new HashMap<>();
            suiteName.put("name", "suite");
            suiteName.put("value", functionality);
            labels.add(suiteName);
            report.replace("labels", labels);
            report.remove("parameters");
            Files.writeString(file.toPath(), gson.toJson(report));
        } catch (IOException e) {
            throw new ConfigFileNotFoundException(e, file.getPath());
        }
    }
}
