package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.rowset.serial.SerialException;
import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCK_DATE;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class UploadFormDataAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private DataRowService dataRowService;

    @Autowired
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.IMPORT_FD;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        String uuid = (String)params.get("uuid");

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                manual,
                logger);

        Long value = formDataService.getValueForCheckLimit(userInfo, formData, getReportType(), uuid, logger);
        String msg = String.format("выбранный файл имеет слишком большой размер (%s Кбайт)!",  value);
        return checkTask(getReportType(), value, formDataService.getTaskName(getReportType(), formDataId, userInfo), msg);
    }

    @Override
    protected boolean executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        String uuid = (String)params.get("uuid");
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        BlobData blobData = blobDataService.get(uuid);
        logger.info("Загрузка данных из файла: \"" + blobData.getName() + "\"");
        //Парсит загруженный в фаловое хранилище xls-файл
        formDataService.importFormData(logger, userInfo,
                formData.getId(), formData.isManual(), blobData.getInputStream(), blobData.getName(), new LockStateLogger() {
                    @Override
                    public void updateState(String state) {
                        lockService.updateState(lock, lockDate, state);
                    }
                });
        // сохраняем данные в основном срезе
        Logger saveLogger = new Logger();
        formDataService.saveFormData(saveLogger, userInfo, formData, true);
        logger.getEntries().addAll(saveLogger.getEntries());
        if (logger.containsLevel(LogLevel.ERROR)) {
            return false;
        }
        return true;
    }

    @Override
    protected String getAsyncTaskName() {
        return "Импорт XLSX-файла";
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
                String.format("Выполнен импорт данных из XLSM файла в экземпляр %s", MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())),
                formData,
                department.getName(),
                false, reportPeriod, rpCompare);
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
                String.format("Не выполнен импорт данных из XLSM файла в экземпляр %s", MessageGenerator.mesSpeckSingleD(formData.getFormType().getTaxType())),
                formData,
                department.getName(),
                manual, reportPeriod, rpCompare);
    }
}
