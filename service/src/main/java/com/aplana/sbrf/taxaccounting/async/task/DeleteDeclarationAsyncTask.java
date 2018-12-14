package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Удаление налоговой формы
 */
@Component("DeleteDeclarationAsyncTask")
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
        taskData.getParams().put("standardDeclarationDescription", declarationDataService.getStandardDeclarationDescription(declarationDataId));
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.DELETE, logger, null);

            // Проверяем ошибки
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException(
                        "Найдены ошибки при выполнении удаления налоговой формы",
                        logEntryService.save(logger.getEntries()));
            }

            deleteReport(declarationDataId, userInfo, false, TaskInterruptCause.DECLARATION_DELETE);
            declarationDataDao.delete(declarationDataId);

            auditService.add(FormDataEvent.DELETE, userInfo, declarationData, "Налоговая форма удалена", null);
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
            return String.format(FAIL, "Удаление", standardDeclarationDescription);
        }
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(getAsyncTaskType().getDescription(),
                declarationDataService.getDeclarationFullName(declarationDataId, getDeclarationDataReportType(userInfo, params)));
    }

    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов связанных с декларациями
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация пользователя
     * @param cause             причина остановки задачи
     */
    private void deleteReport(long declarationDataId, TAUserInfo userInfo, boolean isCalc, TaskInterruptCause cause) {
        DeclarationDataReportType[] ddReportTypes = {DeclarationDataReportType.XML_DEC, DeclarationDataReportType.PDF_DEC, DeclarationDataReportType.EXCEL_DEC, DeclarationDataReportType.CHECK_DEC, DeclarationDataReportType.ACCEPT_DEC};
        for (DeclarationDataReportType ddReportType : ddReportTypes) {
            if (ddReportType.isSubreport()) {
                DeclarationData declarationData = declarationDataDao.get(declarationDataId);
                List<DeclarationSubreport> subreports = declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getSubreports();
                for (DeclarationSubreport subreport : subreports) {
                    ddReportType.setSubreport(subreport);
                    LockData lock = lockDataService.getLock(declarationDataService.generateAsyncTaskKey(declarationDataId, ddReportType));
                    if (lock != null) {
                        asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                    }
                }
            } else if (!isCalc || !DeclarationDataReportType.XML_DEC.equals(ddReportType)) {
                LockData lock = lockDataService.getLock(declarationDataService.generateAsyncTaskKey(declarationDataId, ddReportType));
                if (lock != null) {
                    asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                }
            }
        }
        reportService.deleteAllByDeclarationId(declarationDataId);
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
