package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
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

import java.util.List;
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
        taskData.getParams().put("standardDeclarationDescription", declarationDataService.getStandardDeclarationDescription(declarationDataId));
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            declarationDataScriptingService.executeScript(userInfo, declarationData, FormDataEvent.DELETE, logger, null);

            // Проверяем ошибки
            if (logger.containsLevel(LogLevel.ERROR)) {
                return new BusinessLogicResult(false, null);
            }

            deleteReport(declarationDataId, userInfo, false, TaskInterruptCause.DECLARATION_DELETE);
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
        String standardDeclarationDescription = (String) taskData.getParams().get("standardDeclarationDescription");
        if (isSuccess) {
            return String.format(SUCCESS, standardDeclarationDescription);
        } else {
            return String.format(FAIL, "Удаление", standardDeclarationDescription);
        }
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
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
                    LockData lock = lockDataService.findLock(declarationDataService.generateAsyncTaskKey(declarationDataId, ddReportType));
                    if (lock != null) {
                        asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                    }
                }
            } else if (!isCalc || !DeclarationDataReportType.XML_DEC.equals(ddReportType)) {
                LockData lock = lockDataService.findLock(declarationDataService.generateAsyncTaskKey(declarationDataId, ddReportType));
                if (lock != null) {
                    asyncManager.interruptTask(lock.getTaskId(), userInfo, cause);
                }
            }
        }
        reportService.deleteAllByDeclarationId(declarationDataId);
    }

}
