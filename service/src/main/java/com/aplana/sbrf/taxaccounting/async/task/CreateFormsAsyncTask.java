package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Создание Отчетности
 */
@Component("CreateFormsAsyncTask")
public class CreateFormsAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private AsyncManager asyncManager;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CREATE_FORMS_DEC;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.LONG;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        Integer declarationTypeId = (Integer) params.get("declarationTypeId");
        Long knfId = (Long) params.get("knfId");
        Integer departmentReportPeriodId = (Integer) params.get("departmentReportPeriodId");
        boolean adjustNegativeValues = (Boolean) params.get("adjustNegativeValues");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);

        if (departmentReportPeriod == null) {
            throw new ServiceException("Не удалось определить налоговый период.");
        }

        declarationDataService.createReportForms(knfId, departmentReportPeriod, declarationTypeId, adjustNegativeValues, new LockStateLogger() {
            @Override
            public void updateState(AsyncTaskState state) {
                asyncManager.updateState(taskData.getId(), state);
            }
        }, logger, userInfo);
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        String taskName = generateTaskName(taskData);
        return String.format("Выполнена операция \"%s\"", taskName);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        String taskName = generateTaskName(taskData);
        if (unexpected) {
            Throwable exceptionThrown = (Throwable) taskData.getParams().get("exceptionThrown");
            return String.format("Не выполнена операция \"%s\". Причина: %s", taskName, exceptionThrown.getMessage());
        } else {
            return String.format("Не выполнена операция \"%s\"", taskName);
        }
    }

    private String generateTaskName(AsyncTaskData taskData) {
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        Integer declarationTypeId = (Integer) taskData.getParams().get("declarationTypeId");
        Integer departmentReportPeriodId = (Integer) taskData.getParams().get("departmentReportPeriodId");

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);
        Department department = departmentService.getDepartment(departmentReportPeriod.getDepartmentId());
        int activeDeclarationTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId, departmentReportPeriod.getReportPeriod().getId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(activeDeclarationTemplateId);

        String strCorrPeriod = "";
        if (departmentReportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = " (корр. " + SDF_DD_MM_YYYY.get().format(departmentReportPeriod.getCorrectionDate()) + ")";
        }

        return String.format("Создание отчетных форм: \"%s\", Период: \"%s, %s%s\", Подразделение: \"%s\"",
                declarationTemplate.getName(),
                departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear(),
                departmentReportPeriod.getReportPeriod().getName(),
                strCorrPeriod,
                department.getName()
        );
    }


    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        int declarationTypeId = (Integer) params.get("declarationTypeId");
        int departmentReportPeriodId = (Integer) params.get("departmentReportPeriodId");
        return declarationDataService.getDeclarationFullName(declarationTypeId, departmentReportPeriodId, getAsyncTaskType());
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
