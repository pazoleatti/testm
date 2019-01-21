package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
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
        Map<String, Object> taskParams = taskData.getParams();
        ReportFormsCreationParams params = (ReportFormsCreationParams) taskParams.get("params");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        // TODO 6-НДФЛ сделано по-новому, 2-НДФЛ будет потом
        if (params.getDeclarationTypeId() == DeclarationType.NDFL_6) {
            declarationDataService.createReportForms(params, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            }, logger, userInfo);
        } else {
            declarationDataService.createReportForms2Ndfl(params, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            }, logger, userInfo);
        }
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
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        int declarationTypeId = (Integer) params.get("declarationTypeId");
        int departmentReportPeriodId = (Integer) params.get("departmentReportPeriodId");
        return declarationDataService.getDeclarationFullName(declarationTypeId, departmentReportPeriodId, getAsyncTaskType());
    }

}
