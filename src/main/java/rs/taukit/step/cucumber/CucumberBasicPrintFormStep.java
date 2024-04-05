package rs.taukit.step.cucumber;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.allure.AllureBasicPrintFormStep;
import rs.taukit.utils.PdfUtils;

import static rs.taukit.utils.BasicUtils.eval;

public class CucumberBasicPrintFormStep {
    private AllureBasicPrintFormStep printFormStep(String pageName) {
        return new AllureBasicPrintFormStep(pageName);
    }

    @И("^сформировать эталон \"(.+)\" по файлу \"(.+)\" и шаблону \"(.+)\"$")
    public void createPdfStandard(String standardName, String originFileName, String templateName, DataTable dataTable) {
        PdfUtils.createPdfStandard(eval(standardName), eval(originFileName), eval(templateName), eval(dataTable));
    }

    @И("^сравнить файл \"(.+)\" с эталоном \"(.+)\"$")
    public void comparePdfWithStandard(String testedFileName, String standardName) {
        PdfUtils.comparePdfFiles(eval(testedFileName), eval(standardName));
    }

    /**
     * Сравнивает содержимое pdf-файла с эталонным
     * @param fileName - имя проверяемого файла. Должен находиться в той папке, куда загружаются загруженные файлы
     * @param dataTable - таблица данных, вида<br>
     *                  <table>
     *                      <tr><td>|</td><th>Параметр</th><td>|</td><th>Значение</th><td>|</td></tr>
     *                      <tr><td>|</td><td>эталон</td><td>|</td><td>путь к файлу эталона</td><td>|</td></tr>
     *                      <tr><td>|</td><td>игнорируемые зоны</td><td>|</td><td>путь к yaml-файлу с координатами игнор-зон</td><td>|</td></tr>
     *                      <tr><td>|</td><td>автозамена текста</td><td>|</td><td>путь к yaml-файлу с параметрами замены</td><td>|</td></tr>
     *                  </table>
     *                  Параметры 'игнорируемые зона' и 'автозамена текста' являются не обязательными.
     *                  Путь к файлам может быть относительным к расположению feature-файла сценария.
     */
    @И("^проверить документ \"(.+)\" на соответствие эталону$")
    public void validateDocument(String fileName, DataTable dataTable) {
        printFormStep(fileName).validate(eval(dataTable));
    }
}
