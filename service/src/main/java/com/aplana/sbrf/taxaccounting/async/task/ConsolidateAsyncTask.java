package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.isEmpty;

/**
 * Консолидация налоговой формы
 */
@Component("ConsolidateAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConsolidateAsyncTask extends XmlGeneratorAsyncTask {

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private DepartmentReportPeriodFormatter departmentReportPeriodFormatter;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CONSOLIDATE;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo userInfo, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.LONG;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Date docDate = (Date) taskData.getParams().get("docDate");
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        declarationDataService.consolidate(new TargetIdAndLogger(declarationDataId, logger), userInfo, docDate, null, new LockStateLogger() {
            @Override
            public void updateState(AsyncTaskState state) {
                asyncManager.updateState(taskData.getId(), state);
            }
        });
        if (logger.containsLevel(LogLevel.ERROR)) {
            return new BusinessLogicResult(false, null);
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        Throwable throwable = (Throwable) taskData.getParams().get("exceptionThrown");
        return String.format("Операция \"Консолидация\" не выполнена для формы №: %d, Период: \"%s\", Подразделение: \"%s\".%s",
                declarationDataId,
                departmentReportPeriodFormatter.getPeriodDescription(reportPeriod),
                department.getShortName(),
                throwable != null && !isEmpty(throwable.getMessage())? " Причина: " + throwable.getMessage() : "");
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        return String.format("Операция \"Консолидация\" завершена для формы №: %d, Период: \"%s\", Подразделение: \"%s\"",
                declarationDataId,
                departmentReportPeriodFormatter.getPeriodDescription(reportPeriod),
                department.getShortName());
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        Long declarationDataId = (Long) params.get("declarationDataId");
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        return String.format("\"Консолидация\" для формы №: %d, Период: \"%s, %s%s\", Подразделение: \"%s\"",
                declarationDataId,
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                reportPeriod.getCorrectionDate() != null ? " (корр. " + FastDateFormat.getInstance("dd.MM.yyyy").format(reportPeriod.getCorrectionDate()) + ")" : "",
                department.getName());
    }

    @Override
    protected void sendNotifications(AsyncTaskData taskData, String msg, String uuid, NotificationType notificationType, String reportId) {
        msg = isSuccess(taskData) ? getNotificationMsg(taskData) : getErrorMsg(taskData, true);
        super.sendNotifications(taskData, msg, uuid, notificationType, reportId);
    }

    /**
     * Проверка успешности выполнения асинка
     *
     * @param taskData проверяемая задача
     * @return признак успеха выполнения задачи
     */
    private boolean isSuccess(AsyncTaskData taskData) {
        return taskData.getParams().get("errorsText") == null;
    }

}
