package rs.taukit.step.definition;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import rs.taukit.step.manager.PdfManager;
import rs.taukit.utils.PdfUtils;

import static rs.taukit.utils.BasicUtils.eval;

public class PdfStep extends PdfManager {
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
     *
     * @param fileParameterName - имя параметра, содержащего объект-тестируемый файл.
     * @param dataTable         - таблица данных, вида<br>
     *                          <table>
     *                              <tr><td>|</td><th>Параметр</th><td>|</td><th>Значение</th><td>|</td></tr>
     *                              <tr><td>|</td><td>эталон</td><td>|</td><td>имя параметра, содержащего объект-эталонный файл</td><td>|</td></tr>
     *                              <tr><td>|</td><td>игнорируемые зоны</td><td>|</td><td>путь к yaml-файлу с координатами игнор-зон</td><td>|</td></tr>
     *                              <tr><td>|</td><td>автозамена текста</td><td>|</td><td>путь к yaml-файлу с параметрами замены</td><td>|</td></tr>
     *                          </table>
     *                          Параметры 'игнорируемые зона' и 'автозамена текста' являются не обязательными.
     *                          Путь к файлам может быть относительным к расположению feature-файла сценария.
     */
    @И("^проверить документ \"(.+)\" на соответствие эталону$")
    public void validateDocument(String fileParameterName, DataTable dataTable) {
        validate(fileParameterName, eval(dataTable));
    }

    @И("^конвертировать PDF-файл \"(.+)\" в текстовый формат$")
    public void convertPdfToTxt(String fileParameterName) {
        super.convertPdfToTxt(fileParameterName);
    }

    @И("^сравнить количество страниц в PDF-файле \"(.+)\" с ожидаемым значением \"(.+)\"$")
    public void validatePageCount(String testedFileParameterName, String expectedPageCount){
        validatePdfPageCount(testedFileParameterName, eval(expectedPageCount));
    }
}
