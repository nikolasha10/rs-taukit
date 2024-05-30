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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unchecked")
@Getter
public class AllureReport {
    private static final List<String> errorList = new ArrayList<>();

    private static List<File> getFileList(String functionality, String scenarioName, long testEndTime) {
        File directory = new File(Config.getProperty("allure-results.path"));
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(file -> file.getName().endsWith("result.json"))
                .filter(file -> (file.lastModified() >= testEndTime))
                .filter(file -> {
                    try {
                        String fileContent = Files.readString(file.toPath());
                        boolean containsScenarioName = fileContent.contains(scenarioName.replaceAll("\"", "\\\\\""));
                        boolean containsFunctionality = fileContent.contains(functionality);
                        return containsScenarioName && containsFunctionality;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    public static synchronized void editReports(String scenarioName, String functionality, String status, long testEndTime) {
        List<File> fileList = getFileList(functionality, scenarioName, testEndTime);
        fileList.forEach(file -> replaceReport(file, scenarioName, functionality, status));
    }

    @SuppressWarnings("unchecked")
    private static void replaceReport(File file, String scenarioName, String functionality, String status) {
        try {
            String fileContent = Files.readString(file.toPath());
            Gson gson = new Gson();
            Map<Object, Object> report = gson.fromJson(fileContent, Map.class);
            report.replace("name", scenarioName);
            List<Map<String, String>> labels = new ArrayList<>();
            String format = "(\\[(.+?)])+?";
            Matcher matcher = Pattern.compile(format).matcher(functionality);
            Map<String, String> parentSuitName = new HashMap<>();
            parentSuitName.put("name", "parentSuite");
            if (matcher.find()) {
                parentSuitName.put("value", matcher.group(2));
                labels.add(parentSuitName);
                if (matcher.find()) {
                    Map<String, String> suiteName = new HashMap<>();
                    suiteName.put("name", "suite");
                    suiteName.put("value", matcher.group(2));
                    labels.add(suiteName);
                }
                if (matcher.find()) {
                    Map<String, String> subSuiteName = new HashMap<>();
                    subSuiteName.put("name", "subSuite");
                    subSuiteName.put("value", matcher.group(2));
                    labels.add(subSuiteName);
                }
            } else {
                parentSuitName.put("value", functionality);
                labels.add(parentSuitName);
            }
            report.replace("labels", labels);
            report.remove("parameters");
            String testStatus = report.get("status").toString();
            int warningFlag = checkBrokenSteps(report);
            if (testStatus.equals("passed") && warningFlag == 0) {
                report.replace("status", "unknown");
            } else {
                report.replace("status", status);
            }
            Map<String, String> statusDetails = (Map<String, String>) report.get("statusDetails");
            if (!errorList.isEmpty()) {
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
            errorList.clear();
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
            s.replace("parameters", new ArrayList<>());
            if (s.get("name").toString().contains("Некритическая ошибка:")) {
                s.replace("status", "failed");
                errorList.add(s.get("name").toString()
                        .replace("Некритическая ошибка:", ""));
                resultStatus.set(0);
            }
            if (!s.containsKey("status")) {
                s.put("status", "unknown");
            }
            if (s.get("status").equals("failed")) {
                json.replace("status", "failed");
            }
        });
        return resultStatus.get();
    }
}
