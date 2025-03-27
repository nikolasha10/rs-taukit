package rs.taukit.utils;

import ru.testit.client.model.AutoTestStepModel;
import ru.testit.client.model.LabelPostModel;

import java.util.List;
import java.util.UUID;

public interface CustomAutotestModel {
    void setExternalId(String testId);

    void setProjectId(UUID uuid);

    void setName(String name);

    void setNamespace(String parentSuiteName);

    void setClassname(String suiteName);

    void setSteps(List<AutoTestStepModel> autotestStepModel);

    void setLabels(List<LabelPostModel> labelPostModelList);

    void setId(UUID uuid);
}
