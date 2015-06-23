package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCK_DATE;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class CsvGeneratorAsyncTask extends AbstractAsyncTask {

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
        return ReportType.CSV;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) throws AsyncTaskException {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                manual,
                logger);

        Long value = formDataService.getValueForCheckLimit(userInfo, formData, getReportType(), null);
        String msg = String.format("количество ячеек таблицы формы(%s) превышает максимально допустимое(%s)!", value, "%s");
        return checkTask(getReportType(), value, formDataService.getTaskName(getReportType(), formDataId, userInfo), msg);
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
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
        String uuid = printingService.generateCSV(userInfo, formDataId, manual, isShowChecked, saved, new LockStateLogger() {
            @Override
            public void updateState(String state) {
                lockService.updateState(lock, lockDate, state);
            }
        });
        reportService.create(formDataId, uuid, ReportType.CSV, isShowChecked, manual, saved);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Формирование csv-файла";
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
        Integer periodOrder = formData.getPeriodOrder();
        String strCorrPeriod = "";
        if (reportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate());
        }
        if (periodOrder == null){
            return String.format("Сформирован %s отчет налоговой формы: Период: \"%s, %s%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\"",
                    getReportType().getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), strCorrPeriod, department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format("Сформирован %s отчет налоговой формы: Период: \"%s, %s%s\", Месяц: \"%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\"",
                    getReportType().getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), strCorrPeriod, Formats.getRussianMonthNameWithTier(formData.getPeriodOrder()), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        }
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
        Integer periodOrder = formData.getPeriodOrder();
        String strCorrPeriod = "";
        if (reportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate());
        }
        if (periodOrder == null){
            return String.format("Произошла непредвиденная ошибка при формировании %s отчета налоговой формы: Период: \"%s, %s%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\". Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета",
                    getReportType().getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), strCorrPeriod, department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format("Произошла непредвиденная ошибка при формировании %s отчета налоговой формы: Период: \"%s, %s$s\", Месяц: \"%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\". Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета",
                    getReportType().getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), strCorrPeriod, Formats.getRussianMonthNameWithTier(formData.getPeriodOrder()), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        }
    }
}
