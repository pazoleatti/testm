package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCK_DATE;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class MoveFormDataAsyncTask extends AbstractAsyncTask {

    private static final String SUCCESS = "Успешно переведена из статуса \"%s\" в статус \"%s\" %s:";
    private static final String FAIL = "Не удалось перевести из статуса \"%s\" в статус \"%s\" %s:";

    @Autowired
    private TAUserService userService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.MOVE_FD;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                false,
                logger);

        Long value = formDataService.getValueForCheckLimit(userInfo, formData, getReportType(), null, logger);
        String msg = String.format("количество ячеек таблицы формы(%s) превышает максимально допустимое(%s)!", value, "%s");
        return checkTask(getReportType(), value, formDataService.getTaskName(getReportType(), formDataId, userInfo), msg);
    }

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, final Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        int workflowMoveId = (Integer)params.get("workflowMoveId");
        WorkflowMove move = WorkflowMove.fromId(workflowMoveId);
        final TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        formDataService.doMove(formDataId, false, userInfo, move, null, logger, true, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                lockService.updateState(lock, lockDate, state);
            }
        });
        return new TaskStatus(true, null);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Подготовка/утверждение/принятие НФ";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        int workflowMoveId = (Integer)params.get("workflowMoveId");
        WorkflowMove move = WorkflowMove.fromId(workflowMoveId);

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, false, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativePeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

        return MessageGenerator.getFDMsg(
                String.format(SUCCESS, move.getFromState().getTitle(), move.getToState().getTitle(), formData.getFormType().getTaxType() == TaxType.ETR || formData.getFormType().getTaxType() == TaxType.DEAL ?
                        "форму" : "налоговую форму"),
                formData,
                department.getName(),
                false,
                reportPeriod,
                rpCompare);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        int workflowMoveId = (Integer)params.get("workflowMoveId");
        WorkflowMove move = WorkflowMove.fromId(workflowMoveId);

        boolean manual = false;
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativePeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

        return MessageGenerator.getFDMsg(
                String.format(FAIL, move.getFromState().getTitle(), move.getToState().getTitle(),formData.getFormType().getTaxType() == TaxType.ETR || formData.getFormType().getTaxType() == TaxType.DEAL ?
                        "форму" : "налоговую форму"),
                formData,
                department.getName(),
                false,
                reportPeriod,
                rpCompare);
    }
}