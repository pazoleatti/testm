package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaskInterruptCause;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Удаление налоговой формы
 */
@Component("DeleteDeclarationAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeleteDeclarationAsyncTask extends AbstractDeclarationAsyncTask {

    @Autowired
    private TAUserService userService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DeclarationDataScriptingService declarationDataScriptingService;
    @Autowired
    private DeclarationDataDao declarationDataDao;
    @Autowired
    private AuditService auditService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private ReportService reportService;

    private static final String SUCCESS = "Успешно выполнена операция \"Удаление\" для налоговой формы: %s";

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.DELETE_DEC;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        taskData.getParams().put("fullDeclarationDescription", declarationDataService.getFullDeclarationDescription(declarationDataId));
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.DELETE, logger, null);

            // Проверяем ошибки
            if (logger.containsLevel(LogLevel.ERROR)) {
                return new BusinessLogicResult(false, null);
            }

            deleteReport(declarationDataId, userInfo, TaskInterruptCause.DECLARATION_DELETE);
            declarationDataDao.delete(declarationDataId);

            auditService.add(FormDataEvent.DELETE, userInfo, declarationData, "Налоговая форма удалена", null);
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getTaskLimitMsg(Long value, Map<String, Object> params) {
        return "форма содержит больше ФЛ, чем допустимо. Обратитесь к администратору системы.";
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
        String fullDeclarationDescription = (String) taskData.getParams().get("fullDeclarationDescription");
        if (isSuccess) {
            return String.format(SUCCESS, fullDeclarationDescription);
        } else {
            return String.format(FAIL, "Удаление", fullDeclarationDescription);
        }
    }

    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов связанных с декларациями
     *  @param declarationDataId идентификатор декларации
     * @param userInfo          информация пользователя
     * @param cause             причина остановки задачи
     */
    private void deleteReport(long declarationDataId, TAUserInfo userInfo, TaskInterruptCause cause) {
        // TODO не работает как надо из-за generateAsyncTaskKey
        AsyncTaskType[] asyncTaskTypes = {AsyncTaskType.XML_DEC, AsyncTaskType.PDF_DEC, AsyncTaskType.EXCEL_DEC, AsyncTaskType.CHECK_DEC, AsyncTaskType.ACCEPT_DEC};
        for (AsyncTaskType asyncTaskType : asyncTaskTypes) {
            LockData lock = lockDataService.findLock(declarationDataService.generateAsyncTaskKey(declarationDataId, asyncTaskType));
            if (lock != null) {
                asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
            }
        }
        reportService.deleteAllByDeclarationId(declarationDataId);
    }

}
