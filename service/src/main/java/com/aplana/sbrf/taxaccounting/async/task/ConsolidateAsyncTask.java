package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Консолидация налоговой формы
 */
@Component("ConsolidateAsyncTask")
public class ConsolidateAsyncTask extends XmlGeneratorAsyncTask {

    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

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
        return String.format("Не выполнена консолидация данных в налоговую форму: №: %d, Период: \"%s, %s\", Подразделение: \"%s\", Вид: \"РНУ НДФЛ (консолидированная)",
                declarationDataId,
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                department.getShortName());
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        return String.format("Выполнена консолидация данных в налоговую форму: №: %d, Период: \"%s, %s\", Подразделение: \"%s\", Вид: \"РНУ НДФЛ (консолидированная)",
                declarationDataId,
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                department.getShortName());
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        Long declarationDataId = (Long) params.get("declarationDataId");
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        return String.format("\"Консолидация\" для формы №:%d, Период: \"%s, %s\", Подразделение: \"%s\"",
                declarationDataId,
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                department.getName());
    }

    @Override
    protected void sendNotifications(AsyncTaskData taskData, String msg, String uuid, NotificationType notificationType, String reportId) {
        Long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        msg = String.format("Операция \"Консолидация\" завершена для формы №: %d, Период: \"%s, %s\", Подразделение: \"%s\"",
                declarationDataId,
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                reportPeriod.getReportPeriod().getName(),
                department.getName());
        super.sendNotifications(taskData, msg, uuid, notificationType, reportId);
    }

}
