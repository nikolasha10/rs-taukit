package rs.taukit.utils;

import io.qameta.allure.Step;
import rs.taukit.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static rs.taukit.utils.BasicUtils.testContext;

public class FileUtils {
    @Step("переименовать файл '{fileName}' в '{newFileName}'")
    public static void saveDownloadedFileAs(String fileName, String newFileName) {
        String originalFilePath = String.format("%s/%s/%s",
                Config.getProperty("downloads.dir"),
                testContext().getScenarioName(),
                fileName);
        String filePath = String.format("%s/%s/%s",
                Config.getProperty("downloads.dir"),
                testContext().getScenarioName(),
                newFileName);
        File file = new File(originalFilePath);
        File destinationFile = new File(filePath);
        try {
            Files.move(file.toPath(), destinationFile.toPath(), REPLACE_EXISTING);
            TauLogger.report("Файл сохранен в \"%s\"", destinationFile.getPath());
            TauLogger.attachFile(newFileName, "pdf", destinationFile.getPath());
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось сохранить файл \"%s\", как \"%s\"", fileName, newFileName);
        }
    }

    public static void createFile(String filePath) {
        String[] chunks = filePath.split(File.pathSeparator);
        String dirPath = Arrays.stream(chunks).limit(chunks.length - 1).collect(Collectors.joining(File.pathSeparator));
        try {
            Files.createDirectories(new File(dirPath).toPath());
            File file = new File(filePath);
            if (file.exists()) {
                Files.delete(file.toPath());
            }
            Files.createFile(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(String filePath) {
        File file = new File(filePath);
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void appendFile(String filePath, String text) {
        File file = new File(filePath);
        try {
            Files.writeString(file.toPath(), text, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<File> getDirFileList(String dirPath) {
        HashSet<File> result = new HashSet<>();
        File scannedDir = new File(dirPath);
        File[] fileList = scannedDir.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    Set<File> subDirFileList = getDirFileList(file.getAbsolutePath());
                    result.addAll(subDirFileList);
                } else {
                    result.add(file);
                }
            }
        }
        return result;
    }

    public static String getDownloadFilePath() {
        String childDirName = BasicUtils.testContext().getScenarioName().trim();
        while (childDirName.endsWith(".")) {
            childDirName = childDirName.substring(0, childDirName.length() - 1);
        }
        return String.format("%s/%s", Config.getProperty("downloads.dir"), childDirName);
    }
}
