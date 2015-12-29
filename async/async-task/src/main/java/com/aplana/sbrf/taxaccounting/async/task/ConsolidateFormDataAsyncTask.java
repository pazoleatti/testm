package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

public abstract class ConsolidateFormDataAsyncTask extends AbstractAsyncTask {

    private static final String SUCCESS = "Успешно выполнена консолидация %s:";
    private static final String FAIL = "Не удалось выполнить консолидацию %s:";

    @Autowired
    private TAUserService userService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.CONSOLIDATE_FD;
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
        try {
            formDataService.checkCompose(formData, userInfo, logger);
        } catch (ServiceException e) {
            String errorMsg = String.format(LockData.CHECK_TASK,
                    formDataService.getTaskName(ReportType.CONSOLIDATE_FD, formData.getId(), userInfo),
                    e.getMessage());
            throw new AsyncTaskException(new ServiceLoggerException(errorMsg,
                    logEntryService.save(logger.getEntries())));
        }
        Long value = formDataService.getValueForCheckLimit(userInfo, formData, getReportType(), null, logger);
        String msg = String.format("сумма количества ячеек таблицы формы по всем формам источникам (%s) превышает максимально допустимое (%s)!", value, "%s");
        return checkTask(getReportType(), value, formDataService.getTaskName(getReportType(), formDataId, userInfo), msg);
    }

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                false,
                logger);
        try {
            formDataService.checkCompose(formData, userInfo, logger);
        } catch (ServiceException e) {
            String errorMsg = String.format(LockData.CHECK_TASK,
                    formDataService.getTaskName(ReportType.CONSOLIDATE_FD, formData.getFormType().getId(), userInfo),
                    e.getMessage());
            throw new ServiceException(errorMsg);
        }
        formDataService.compose(formData, userInfo, logger, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                lockService.updateState(lock, lockDate, state);
            }
        });
        return new TaskStatus(true, null);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Конслолидация НФ";
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
        DepartmentReportPeriod rpCompare = formData.getComparativePeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

        return MessageGenerator.getFDMsg(
                String.format(SUCCESS, MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())),
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
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, false, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        DepartmentReportPeriod rpCompare = formData.getComparativePeriodId() != null ?
                departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

        return MessageGenerator.getFDMsg(
                String.format(FAIL, MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())),
                formData,
                department.getName(),
                false,
                reportPeriod,
                rpCompare);
    }
}
