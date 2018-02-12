package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Идентификация налоговой формы
 */
@Component("IdentifyAsyncTask")
public class IdentifyAsyncTask extends XmlGeneratorAsyncTask {
    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.IDENTIFY_PERSON;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        try {
            Date docDate = (Date) taskData.getParams().get("docDate");
            long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
            TAUserInfo userInfo = new TAUserInfo();
            userInfo.setUser(userService.getUser(taskData.getUserId()));
            declarationDataService.identify(new TargetIdAndLogger(declarationDataId, logger), userInfo, docDate, null, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            });
            return new BusinessLogicResult(true, null);
        } catch (AccessDeniedException e) {
            throw new ServiceException("");
        }
    }
}
