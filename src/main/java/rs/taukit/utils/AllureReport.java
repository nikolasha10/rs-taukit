package rs.taukit.utils;

import com.google.gson.Gson;
import lombok.Getter;
import rs.taukit.config.Config;
import rs.taukit.exceptions.ConfigFileNotFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
@Getter
public class AllureReport {
    private static List<String> errorList = new ArrayList<>();
    private static List<File> getFileList(String scenarioName, long testEndTime) {
        File directory = new File(Config.getProperty("allure-results.path"));
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(file -> file.getName().endsWith("result.json"))
                .filter(file -> (file.lastModified() >= testEndTime))
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
            String testStatus = report.get("status").toString();
            int warningFlag = checkBrokenSteps(report);
            if (testStatus.equals("passed") && warningFlag == 0) {
                report.replace("status", "unknown");
            }
            Map<String, String> statusDetails = (Map<String, String>) report.get("statusDetails");
            if (errorList.size() > 0) {
                String notes = "\n" + IntStream.range(1, errorList.size() + 1).boxed()
                        .map(i -> String.format("%d. %s", i, errorList.get(i - 1)))
                        .collect(Collectors.joining("\n"));
                if (statusDetails.get("message") == null) {
                    statusDetails.put("message",
                            "Тест пройден с некритическими ошибками: " + notes);
                } else {
                    statusDetails.put("message", String.format("%s\n%s %s", statusDetails.get("message"),
                            "Также, выявлены некритические ошибки:", notes));
                }
            }
            Files.writeString(file.toPath(), gson.toJson(report));
        } catch (IOException e) {
            throw new ConfigFileNotFoundException(e, file.getPath());
        }
    }

    private static int checkBrokenSteps(Map json) {
        List<Map> steps = (List<Map>) json.get("steps");
        AtomicInteger resultStatus = new AtomicInteger(1);
        steps.forEach(s -> {
            resultStatus.set(checkBrokenSteps(s) * resultStatus.get());
            if (s.get("name").toString().contains("Некритическая ошибка:")) {
                s.replace("status", "failed");
                errorList.add(s.get("name").toString()
                        .replace("Некритическая ошибка:", ""));
                resultStatus.set(0);
            }
            if (s.get("status").equals("failed")) {
                json.replace("status", "failed");
            }
        });
        return resultStatus.get();
    }
}
