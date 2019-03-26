package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.NdflPersonService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("UpdatePersonsDataAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpdatePersonsDataAsyncTask extends AbstractDeclarationAsyncTask {

    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private TAUserService userService;
    @Autowired
    private NdflPersonService ndflPersonService;


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
    protected String getTaskLimitMsg(Long value, Map<String, Object> params) {
        return "форма содержит больше ФЛ, чем допустимо. Обратитесь к администратору системы.";
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
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

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo userInfo, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        Long declarationDataId = (Long) params.get("declarationDataId");
        long value = ndflPersonService.getNdflPersonCount(declarationDataId);
        return checkTask(value, taskDescription, getTaskLimitMsg(value, params));
    }

}
