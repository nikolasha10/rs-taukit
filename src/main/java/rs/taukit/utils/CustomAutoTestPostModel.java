package rs.taukit.utils;

import ru.testit.client.model.AutoTestPostModel;

import java.util.UUID;

public class CustomAutoTestPostModel extends AutoTestPostModel implements CustomAutotestModel {
    @Override
    public void setId(UUID uuid) {
        TauLogger.error("Необходимо удалить параметр TestITId из тестовых данных сценария '%s'", this.getName());
    }
}
