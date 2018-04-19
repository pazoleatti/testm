package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("UpdatePersonsDataAsyncTask")
public class UpdatePersonsDataAsyncTask extends AbstractDeclarationAsyncTask {

    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private TAUserService userService;


    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) throws InterruptedException {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        declarationDataService.performUpdatePersonsData(declarationDataId, logger, userInfo);
        if (logger.containsLevel(LogLevel.ERROR)) {
            return new BusinessLogicResult(false, null);
        }

        return new BusinessLogicResult(true, null);
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return declarationDataService.getDeclarationFullName(declarationDataId, getDeclarationDataReportType(userInfo, params));
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return String.format("Завершено обновление данных ФЛ налоговой формы: %s." ,
                getDeclarationDescription(taskData.getUserId(), taskData.getParams()));
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return getNotificationMsg(taskData);
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.UPDATE_PERSONS_DATA;
    }
}
