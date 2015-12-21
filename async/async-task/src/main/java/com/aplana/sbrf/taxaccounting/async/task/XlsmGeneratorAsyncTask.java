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

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

public abstract class XlsmGeneratorAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private PrintingService printingService;

    @Autowired
    private FormDataAccessService formDataAccessService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.EXCEL;
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
    protected boolean executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        boolean isShowChecked = (Boolean)params.get("isShowChecked");
        boolean saved = (Boolean)params.get("saved");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        formDataAccessService.canRead(userInfo, formDataId);
        String uuid = printingService.generateExcel(userInfo, formDataId, manual, isShowChecked, saved, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                lockService.updateState(lock, lockDate, state);
            }
        });
        reportService.create(formDataId, uuid, FormDataReportType.EXCEL, isShowChecked, manual, saved);
        return true;
    }

    @Override
    protected String getAsyncTaskName() {
        return "Формирование xlsm-файла";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        boolean isShowChecked = (Boolean)params.get("isShowChecked");
        boolean saved = (Boolean)params.get("saved");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativePeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

        return MessageGenerator.getFDMsg(
                String.format(COMPLETE_FORM, getReportType().getName()),
                formData,
                department.getName(),
                manual, reportPeriod, rpCompare, isShowChecked, saved);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        boolean isShowChecked = (Boolean)params.get("isShowChecked");
        boolean saved = (Boolean)params.get("saved");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativePeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

        return MessageGenerator.getFDMsg(String.format(ERROR_FORM, getReportType()),
                formData,
                department.getName(),
                manual, reportPeriod, rpCompare, isShowChecked, saved);
    }
}
