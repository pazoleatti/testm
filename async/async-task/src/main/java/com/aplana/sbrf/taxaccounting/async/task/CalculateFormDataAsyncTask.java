package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class CalculateFormDataAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private LogEntryService logEntryService;

    @Override
    protected ReportType getReportType() {
        return ReportType.CALCULATE_FD;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                manual,
                logger);

        Long value = formDataService.getValueForCheckLimit(userInfo, formData, getReportType(), null, logger);
        String msg = String.format("количество ячеек таблицы формы(%s) превышает максимально допустимое(%s)!", value, "%s");
        return checkTask(getReportType(), value, formDataService.getTaskName(getReportType(), formDataId, userInfo), msg);
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                manual,
                logger);
        formDataService.doCalc(logger, userInfo, formData);
        // сохраняем данные в основном срезе
        formDataService.saveFormData(logger, userInfo, formData);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Расчет данных налоговой формы";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, false, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativPeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativPeriodId()) : null;

        return MessageGenerator.getInfoFDMsg(
                String.format(COMPLETE_FORM, getReportType().getName()),
                formData.getFormType().getName(),
                formData.getKind().getName(),
                department.getName(),
                formData.getComparativPeriodId(),
                formData.getPeriodOrder(), false, reportPeriod, rpCompare);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, false, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativPeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativPeriodId()) : null;

        return MessageGenerator.getErrorFDMsg(String.format(ERROR_FORM, getReportType()), formData, false, department.getName(), reportPeriod, rpCompare);
    }
}
