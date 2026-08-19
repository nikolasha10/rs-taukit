package rs.taukit.utils;

import com.google.gson.Gson;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import rs.taukit.config.Config;
import rs.taukit.exceptions.ConfigFileNotFoundException;
import rs.taukit.scenario.CucumberScenario;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static rs.taukit.utils.BasicUtils.scenario;
import static rs.taukit.utils.BasicUtils.storage;

@SuppressWarnings("unchecked")
@Getter
public class AllureReport {
    private static final List<String> errorList = new ArrayList<>();
    private static final Set<String> categories = new HashSet<>();

    private static List<File> getFileList(String functionality, String scenarioName, long testEndTime) {
        File directory = new File(Config.getProperty("allure-results.path"));
        return Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                .filter(file -> file.getName().endsWith("result.json"))
                .filter(file -> (file.lastModified() >= testEndTime - 1000))
                .filter(file -> {
                    try {
                        String fileContent = Files.readString(file.toPath());
                        boolean containsScenarioName = fileContent.contains(scenarioName.replaceAll("\"", "\\\\\""));
                        boolean containsFunctionality = fileContent.contains(functionality);
                        return containsScenarioName && containsFunctionality;
                    } catch (IOException e) {
                        TauLogger.console("Не удалось проанализировать содержимое файла %s из-за ошибки:", file.getName());
                        TauLogger.console(e);
                        return false;
                    }
                }).toList();
    }

    private static List<String> getCategories() {
        String categoriesPath = Config.getProperty("allure-results.path") + "/categories.json";
        File file = new File(categoriesPath);
        if (!file.exists()) return Collections.emptyList();
        String categoriesText = FileUtils.readFile(categoriesPath);
        List<Map<String, Object>> categoriesList = JsonParser.parse(categoriesText);
        return categoriesList.stream().map(category -> (String) category.get("name")).toList();
    }

    public static synchronized void editReports(CucumberScenario scenario, String status, long testEndTime) {
        storage.saveErrorData(new File("target/temp/errors.yml"));
        String functionality = scenario.getFunctionality();
        String scenarioName = scenario.getName();
        String currentFunctionality = scenario.getCurrentFunctionality();
        String currentScenarioName = scenario.getCurrentName();
        List<String> tagList = scenario.getTagList();
        List<File> fileList = getFileList(currentFunctionality, currentScenarioName, testEndTime);
        categories.addAll(getCategories());
        fileList.forEach(file -> replaceReport(file, scenarioName, functionality, status, tagList));
    }

    @SuppressWarnings("unchecked")
    private static void replaceReport(File file, String scenarioName, String functionality, String status, List<String> tagList) {
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
            String projectFullName = Config.getProperty("project.full.name");
            if (projectFullName != null) {
                report.replace("fullName", Config.getProperty("project.full.name"));
            }
            report.replace("historyId", DigestUtils.md5Hex(scenario().getTestId()));
            report.replace("labels", labels);
            report.put("links", getLinksFromTags(tagList));
            report.remove("parameters");
            String testStatus = report.get("status").toString();
            int warningFlag = checkBrokenSteps(report);
            if (testStatus.equals("passed") && warningFlag == 0) {
                report.replace("status", "unknown");
            } else if (warningFlag == 2) {
                report.replace("status", "broken");
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
            List<Map<String, Object>> categoriesMap = new ArrayList<>();
            categories.forEach(category -> {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("name", category);
                categoryMap.put("matchedStatuses", Arrays.asList("failed", "broken"));
                categoryMap.put("traceRegex", ".*Категории.*" + category + ".*");
                categoriesMap.add(categoryMap);
            });
            Path categoriesPath = Path.of(Config.getProperty("allure-results.path"), "categories.json");
            if (!categoriesMap.isEmpty()) {
                FileUtils.writeFile(categoriesPath, JsonParser.write(categoriesMap));
            }
        } catch (IOException e) {
            throw new ConfigFileNotFoundException(e, file.getPath());
        }
    }

    private static List<Map<String, String>> getLinksFromTags(List<String> tagList) {
        List<Map<String, String>> links = new ArrayList<>();
        tagList.forEach(tag -> {
            Matcher matcher = Pattern.compile("@(.+)\\(\"(.+)\",\"(.+)\"\\)").matcher(tag);
            if (matcher.matches()) {
                Map<String, String> link = new HashMap<>();
                link.put("name", matcher.group(2));
                link.put("url", matcher.group(3));
                link.put("type", matcher.group(1));
                links.add(link);
            }
        });
        return links;
    }

    private static int checkBrokenSteps(Map json) {
        List<Map> steps = (List<Map>) json.get("steps");
        AtomicInteger resultStatus = new AtomicInteger(1);
        steps.forEach(s -> {
            resultStatus.set(checkBrokenSteps(s) * resultStatus.get());
            s.replace("parameters", new ArrayList<>());
            String stepName = s.get("name").toString();
            if (stepName.contains("Некритическая ошибка:")) {
                s.replace("status", "failed");
                String errorMessage = stepName.replace("Некритическая ошибка:", "");
                errorList.add(errorMessage);
                Map<String, String> statusDetails = Map.of(
                        "known", "false",
                        "muted", "false",
                        "flaky", "false",
                        "message", errorMessage,
                        "trace", ""
                );
                resultStatus.set(0);
            }
            if (!s.containsKey("status")) {
                s.put("status", "unknown");
            }
            if (s.get("status").equals("failed")) {
                json.replace("status", "failed");
            } else if (s.get("status").equals("broken")) {
                json.replace("status", "broken");
                resultStatus.set(2);
            }
            Map<String, String> details = (Map<String, String>) s.get("statusDetails");
            String errorMessage = "";
            if (details != null) {
                errorMessage = details.get("message");
                String trace = details.get("trace");
                Matcher matcher = Pattern.compile("Категории: (.+?)(\\r)?\\n").matcher(trace);
                if (matcher.find()) {
                    categories.addAll(Arrays.asList(matcher.group(1).trim().split(", ")));
                }
                matcher = Pattern.compile("Шаг: (.+?)(\\r)?\\n").matcher(trace);
                if (matcher.find()) {
                    stepName = matcher.group(1).trim();
                }
            }
            details = (Map<String, String>) json.get("statusDetails");
            Map<String, String> errorData = YamlParser.convert(storage.getErrorData(scenario().getFullName(), stepName, errorMessage));
            if (errorData != null && !errorData.get("Статус").isEmpty() && details != null) {
                details.put("status", errorData.get("Статус"));
                details.put("note", errorData.get("Примечание"));
                details.compute("trace", (k, trace) -> trace + " Категории: " + errorData.get("Статус") + "\n");
                s.replace("statusDetails", details);
                categories.add(errorData.get("Статус"));
            }
        });

        return resultStatus.get();
    }

    public static Map<String, ?> getAllureResultModel(String filePath) {
        return JsonParser.parse(FileUtils.readFile(filePath));
    }
}
