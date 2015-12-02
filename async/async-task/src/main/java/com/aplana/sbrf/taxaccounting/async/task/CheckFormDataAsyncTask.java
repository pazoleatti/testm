package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class CheckFormDataAsyncTask extends AbstractAsyncTask {

    private static final String MESSAGE = "Выполнена проверка %s:";

    @Autowired
    private TAUserService userService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Override
    protected ReportType getReportType() {
        return ReportType.CHECK_FD;
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
    protected boolean executeBusinessLogic(Map<String, Object> params, final Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        final TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
		formDataService.doCheck(logger, userInfo, formData, false);
        return true;
	}

    @Override
    protected String getAsyncTaskName() {
        return "Проверка данных налоговой формы";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativePeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

        return MessageGenerator.getFDMsg(
                String.format(MESSAGE, MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())),
                formData,
                department.getName(),
                manual,
                reportPeriod,
                rpCompare);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativePeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

        return MessageGenerator.getFDMsg(
                String.format(MESSAGE, MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())),
                formData,
                department.getName(),
                manual,
                reportPeriod,
                rpCompare);
    }
}
