package rs.taukit.utils;

import io.qameta.allure.Step;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import rs.taukit.config.Config;
import rs.taukit.exceptions.FileNotCreatedException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static rs.taukit.utils.BasicUtils.*;

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
            TauLogger.report("Файл сохранен в '%s'", destinationFile.getPath());
            TauLogger.attachPdfFile(newFileName, destinationFile.getPath());
        } catch (Exception e) {
            TauLogger.fail(e, "Не удалось сохранить файл '%s', как '%s'", fileName, newFileName);
        }
    }

    public static void createFile(String filePath) {
        File tmpFile = new File(filePath);
        String[] chunks = tmpFile.getAbsolutePath().split("[\\\\/]");
        String dirPath = Arrays.stream(chunks).limit(chunks.length - 1).collect(Collectors.joining("/"));
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
        if (!file.exists()) {
            File featureFile = new File(scenarioData().getFeaturePath());
            File localFile = new File(featureFile.getParent() + File.separator + filePath);
            if (localFile.exists()) {
                file = localFile;
            }
        }
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(String filePath, String encoding) {
        File file = new File(filePath);
        if (!file.exists()) {
            File featureFile = new File(scenarioData().getFeaturePath());
            File localFile = new File(featureFile.getParent() + File.separator + filePath);
            if (localFile.exists()) {
                file = localFile;
            }
        }
        try {
            return Files.readString(file.toPath(), Charset.forName(encoding));
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

    public static void writeFile(String filePath, String content) {
        createFile(filePath);
        File file = new File(filePath);
        try {
            Files.writeString(file.toPath(), content);
        } catch (IOException e) {
            throw new FileNotCreatedException(e, filePath);
        }

    }

    public static void writeFile(Path filePath, byte[] content) {
        try {
            Files.write(filePath, content);
        } catch (IOException e) {
            throw new FileNotCreatedException(e, filePath.toString());
        }

    }

    public static void writeFile(String filePath, String content, String encoding) {
        createFile(filePath);
        File file = new File(filePath);
        try {
            Files.writeString(file.toPath(), content, Charset.forName(encoding));
        } catch (IOException e) {
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
        String downloadDirPath = Config.getProperty("downloads.dir");
        String functionality = scenario().getFunctionality();
        Matcher matcher = Pattern.compile("\\[(.+)]\\[(.+)]").matcher(functionality);
        String suites = functionality;
        if (matcher.matches()) {
            suites = String.format("%s/%s", matcher.group(1), matcher.group(2));
        }
        String scenarioName = scenario().getName();
        while (scenarioName.endsWith(".")) {
            scenarioName = scenarioName.substring(0, scenarioName.length() - 1);
        }
        String hash = DigestUtils.md5Hex(String.format("%s/%s", suites, scenarioName));
        return String.format("%s/%s", downloadDirPath, hash);
    }

    public static String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }

    public static void createDirectories(String filePath) {
        File file = new File(filePath);
        try {
            Files.createDirectories(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> compareTextFiles(String testedFilePath, String standardFilePath) {
        List<String> errorsList = new ArrayList<>();
        String testedFileContent = readFile(testedFilePath);
        String standardFileContent = readFile(standardFilePath);
        if (!testedFileContent.equals(standardFileContent)) {
            List<String> testedFileRows = List.of(testedFileContent.split("\n"));
            List<String> standardFileRows = List.of(standardFileContent.split("\n"));
            int testedFileRowsSize = testedFileRows.size();
            int standardFileRowsSize = standardFileRows.size();
            if (testedFileRowsSize != standardFileRowsSize) {
                errorsList.add(String.format("Количество строк (%d) не равно ожидаемому (%d)",
                        testedFileRowsSize, standardFileRowsSize));
            }
            List<Integer> differentRows = new ArrayList<>();
            for (int i = 0; i < Math.min(testedFileRowsSize, standardFileRowsSize); i++) {
                if (!testedFileRows.get(i).equals(standardFileRows.get(i))) {
                    differentRows.add(i + 1);
                }
            }
            if (!differentRows.isEmpty()) {
                errorsList.add(String.format("Содержимое строк %s отличается от ожидаемого", differentRows));
            }
        }
        return errorsList;
    }

    public static void replaceValues(String filePath, String instructionPath) {
        StringBuilder builder = new StringBuilder(readFile(filePath));
        List<Map<String, String>> instruction = YamlParser.parse(instructionPath);
        for (Map<String, String> item : instruction) {
            try {
                builder = new StringBuilder(builder.toString().replaceAll(item.get("текст"), eval(item.get("замена"))));
            } catch (Exception ignored) {
                TauLogger.console("Не удалось заменить '%s' на '%s'", item.get("текст"), eval(item.get("замена")));
            }
        }
        writeFile(filePath, eval(builder.toString()));
    }

    public static String getTestFilePath(String filePath) {
        String fullPath = Config.getProperty("baseResourcesDir") + "files/" + filePath;
        File file = new File(fullPath);
        return file.getPath();
    }

    public static String getScenarioTempDir(String parentSuiteName, String suiteName, String subSuiteName, String scenarioName) {
        String tempDirPath = String.format("target/temp/%s%s%s/%s",
                parentSuiteName,
                suiteName.equals("-") ? "" : "/" + suiteName,
                subSuiteName.equals("-") ? "" : "/" + subSuiteName,
                scenarioName);
        File file = new File(tempDirPath);
        return file.getAbsolutePath().replaceAll("\\\\", "/");
    }

    public static int getPdfPageCount(String filePath) {
        File file = new File(filePath);

        try (PDDocument pdfDocument = PDDocument.load(file)) {
            return pdfDocument.getNumberOfPages();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Ошибка при обработке PDF-файла '%s': %s", filePath, e.getMessage()));
        }
    }

    public static String getFileType(String fileExtension) {
        switch (fileExtension) {
            case "xls" -> {
                return "application/vnd.ms-excel";
            }
            case "doc" -> {
                return "application/msword";
            }
            case "pdf" -> {
                return "application/pdf";
            }
            case "xml" -> {
                return "application/xml";
            }
            default -> {
                return "text/plain";
            }
        }
    }

    public static void deleteDirectory(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exception) {
            TauLogger.console(exception);
        }
    }
}
