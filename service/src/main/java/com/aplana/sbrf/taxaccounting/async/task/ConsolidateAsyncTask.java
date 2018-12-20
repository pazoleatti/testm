package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        try {
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
        } catch (AccessDeniedException e) {
            throw new ServiceException("");
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

    @Override
    public LockData establishLock(String lockKey, TAUserInfo user, Map<String, Object> params) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean prohibitiveLockExists(Map<String, Object> params, Logger logger) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
