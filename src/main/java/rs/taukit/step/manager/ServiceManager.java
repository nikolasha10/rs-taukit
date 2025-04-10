package rs.taukit.step.manager;

import io.cucumber.datatable.DataTable;
import rs.taukit.config.Config;
import rs.taukit.element.interfaces.IElement;
import rs.taukit.scenario.CucumberScenario;
import rs.taukit.scenario.ScenarioManager;
import rs.taukit.testobject.TestCondition;
import rs.taukit.utils.FileUtils;
import rs.taukit.utils.TauLogger;
import rs.taukit.utils.TestITUtils;
import rs.taukit.utils.YamlParser;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static rs.taukit.utils.BasicUtils.element;
import static rs.taukit.utils.BasicUtils.eval;

public class ServiceManager extends StepManager {
    public void skipActionsIfTrue(DataTable dataTable) {
        run(() -> {
            Predicate<Map<String, String>> predicate = condition -> new TestCondition(condition).checkResult().isConditionMet();
            if (dataTable.asMaps().stream().allMatch(predicate)) {
                scenarioData.put("SKIP_ACTIONS", true);
            }
        });
    }

    public void stopSkipActions() {
        Runnable runnable = () -> scenarioData.remove("SKIP_ACTIONS");
        runnable.run();
        scenarioStorage.saveStepAction(runnable);
    }

    public void createReport() {
        scenarioData.put("scenarioData", ScenarioManager.getInstance().generateFinalReport());
        scenarioData.put("jenkinsPipelineUrl", Config.getProperty("jenkins.pipeline.url"));
        scenarioData.put("jenkinsAuth", Config.getProperty("jenkins.auth"));
        List<Map<String, ?>> standList = YamlParser.parse(Config.eval("${baseResourcesDir}/configs/stands.yaml"));
        String stands = standList.stream().map((item -> "'" + item.get("Название").toString() + "'")).toList().toString();
        scenarioData.put("stands", stands);
        scenarioData.put("currentStand", Config.getSTAND().getName());
        scenarioData.put("branches", Config.getProperty("branches"));
        scenarioData.put("defaultBranch", Config.getProperty("defaultBranch"));
        String template = FileUtils.readFile("src/main/resources/templates/finalReportTemplate.html");
        FileUtils.writeFile("target/temp/finalReport.html", eval(template));
        TauLogger.attachFile("Отчёт", "target/temp/finalReport.html");
    }

    protected void forceClickElement(String elementName) {
        IElement element = element(elementName);
        element.forceClick();
        TauLogger.report("[примечание] нажата ЛКМ над элементом '%s'", elementName);
    }

    protected void createTestItAutotest() {
        List<CucumberScenario> scenarioList = ScenarioManager.getRequestedScenarioList();
        scenarioList.forEach(TestITUtils::createAutotest);
    }

    protected void updateTestItAutotest() {
        List<CucumberScenario> scenarioList = ScenarioManager.getRequestedScenarioList();
        scenarioList.forEach(TestITUtils::updateAutotest);
    }

    protected void setMutex(String mutexName) {
        scenario.setMutex(mutexName);
    }

    protected void releaseMutex(String mutexName) {
        scenario.releaseMutex(mutexName);
    }
}
