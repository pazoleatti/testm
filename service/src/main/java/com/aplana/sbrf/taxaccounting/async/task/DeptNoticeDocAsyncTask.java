package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Задача формирования "Уведомления о задолженности"
 */
@Component("DeptNoticeDocAsyncTask")
public class DeptNoticeDocAsyncTask extends AbstractDeclarationAsyncTask {

    private static final String SUCCESS = "Сформировано \"Уведомление о задолженности\" для формы: %s";
    private static final String FAIL = "Произошла непредвиденная ошибка при формировании \"Уведомление о задолженности\" формы: %s Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета";

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.DEPT_NOTICE_DEC;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        DataRow<Cell> selectedPerson = (DataRow<Cell>) taskData.getParams().get("selectedRecord");

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            String reportId = declarationDataService.createDocReportByPerson(declarationData, selectedPerson, userInfo, logger);
            return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, reportId);
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return getMessage(taskData, false, unexpected);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return getMessage(taskData, true, false);
    }

    private String getMessage(AsyncTaskData taskData, boolean isSuccess, boolean unexpected) {
        String template = isSuccess ? SUCCESS : FAIL;
        return String.format(template,
                getDeclarationDescription(taskData.getUserId(), taskData.getParams()));
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(getAsyncTaskType().getDescription(),
                declarationDataService.getDeclarationFullName(declarationDataId, getDeclarationDataReportType(userInfo, params)));
    }

    @Override
    public LockData lockObject(String lockKey, TAUserInfo user, Map<String, Object> params) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean checkLocks(Map<String, Object> params, Logger logger) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
