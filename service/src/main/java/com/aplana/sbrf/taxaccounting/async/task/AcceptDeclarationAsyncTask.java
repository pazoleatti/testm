package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Принятие налоговой формы
 */
@Component("AcceptDeclarationAsyncTask")
public class AcceptDeclarationAsyncTask extends AbstractDeclarationAsyncTask {

    private static final String SUCCESS = "Успешно выполнено принятие налоговой формы: %s";

    @Autowired
    private TAUserService userService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private AsyncManager asyncManager;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.ACCEPT_DEC;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        taskData.getParams().put("standardDeclarationDescription", declarationDataService.getStandardDeclarationDescription(declarationDataId));
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            declarationDataService.accept(logger, declarationDataId, userInfo, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            });
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        String message = getMessage(taskData, false);
        Exception e = (Exception) taskData.getParams().get("exceptionThrown");
        if (e != null) {
            message = message + String.format(CAUSE, e.toString());
        }
        return message;
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return getMessage(taskData, true);
    }

    private String getMessage(AsyncTaskData taskData, boolean isSuccess) {
        String standardDeclarationDescription = (String) taskData.getParams().get("standardDeclarationDescription");
        if (isSuccess) {
            return String.format(SUCCESS, standardDeclarationDescription);
        } else {
            return String.format(FAIL, "Принятие", standardDeclarationDescription);
        }
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(getAsyncTaskType().getDescription(),
                declarationDataService.getDeclarationFullName(declarationDataId, getDeclarationDataReportType(userInfo, params)));
    }
}
