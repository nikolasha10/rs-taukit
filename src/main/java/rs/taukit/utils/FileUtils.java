package rs.taukit.utils;

import io.qameta.allure.Step;
import rs.taukit.config.Config;
import rs.taukit.exceptions.FileNotCreatedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static rs.taukit.utils.BasicUtils.scenario;

public class FileUtils {
    @Step("переименовать файл '{fileName}' в '{newFileName}'")
    public static void saveDownloadedFileAs(String fileName, String newFileName) {
        String originalFilePath = String.format("%s/%s/%s",
                Config.getProperty("downloads.dir"),
                scenario().getName(),
                fileName);
        String filePath = String.format("%s/%s/%s",
                Config.getProperty("downloads.dir"),
                scenario().getName(),
                newFileName);
        File file = new File(originalFilePath);
        File destinationFile = new File(filePath);
        try {
            Files.move(file.toPath(), destinationFile.toPath(), REPLACE_EXISTING);
            TauLogger.report("Файл сохранен в \"%s\"", destinationFile.getPath());
            TauLogger.attachPdfFile(newFileName, destinationFile.getPath());
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось сохранить файл \"%s\", как \"%s\"", fileName, newFileName);
        }
    }

    public static void createFile(String filePath) {
        File tmpFile = new File(filePath);
        String[] chunks = tmpFile.getAbsolutePath().split("[\\\\/]");
        String dirPath = Arrays.stream(chunks).limit(chunks.length - 1).collect(Collectors.joining(File.separator));
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
    public static List<String> readFileToLineList(String filePath) {
        File file = new File(filePath);
        try {
            return Files.readAllLines(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFile(String filePath, String content){
        File file = new File(filePath);
        try {
            Files.writeString(file.toPath(), content);
        } catch (IOException e){
            throw new FileNotCreatedException(e, filePath);
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
        String childDirName = scenario().getName();
        while (childDirName.endsWith(".")) {
            childDirName = childDirName.substring(0, childDirName.length() - 1);
        }
        return String.format("%s/%s", Config.getProperty("downloads.dir"), childDirName);
    }

    public static String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }

    public static void createDirectories(String filePath){
        File file = new File(filePath);
        try {
            Files.createDirectories(file.toPath());
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static List<String> compareTextFiles(String testedFilePath, String standardFilePath) {
        List<String> errorsList = new ArrayList<>();
        String testedFileContent = readFile(testedFilePath);
        String standardFileContent = readFile(standardFilePath);
        if (!testedFileContent.equals(standardFileContent)){
            List<String> testedFileRows = List.of(testedFileContent.split("\n"));
            List<String> standardFileRows = List.of(standardFileContent.split("\n"));
            int testedFileRowsSize = testedFileRows.size();
            int standardFileRowsSize = standardFileRows.size();
            if (testedFileRowsSize != standardFileRowsSize){
                errorsList.add(String.format("Количество строк (%d) не равно ожидаемому (%d)",
                        testedFileRowsSize, standardFileRowsSize));
            }
            List<Integer> differentRows = new ArrayList<>();
            for (int i = 0; i < Math.min(testedFileRowsSize, standardFileRowsSize); i++) {
                if (!testedFileRows.get(i).equals(standardFileRows.get(i))){
                    differentRows.add(i + 1);
                }
            }
            if (!differentRows.isEmpty()){
                errorsList.add(String.format("Содержимое строк %s отличается от ожидаемого", differentRows));
            }
        }
        return errorsList;
    }
}
