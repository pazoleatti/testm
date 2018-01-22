package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Консолидация налоговой формы
 */
@Component("ConsolidateAsyncTask")
public class ConsolidateAsyncTask extends XmlGeneratorAsyncTask {

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CONSOLIDATE;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Date docDate = (Date) taskData.getParams().get("docDate");
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        declarationDataService.consolidate(logger, declarationData, userInfo, docDate, null, new LockStateLogger() {
            @Override
            public void updateState(AsyncTaskState state) {
                asyncManager.updateState(taskData.getId(), state);
            }
        });
        return new BusinessLogicResult(true, null);
    }
}
