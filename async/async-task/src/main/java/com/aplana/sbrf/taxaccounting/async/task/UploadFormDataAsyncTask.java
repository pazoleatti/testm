package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
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
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockService;

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) throws AsyncTaskException {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        String uuid = (String)params.get("uuid");

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                manual,
                logger);
        Pair<BalancingVariants, Long> checkTaskLimit = formDataService.checkTaskLimit(userInfo, formData, ReportType.IMPORT_FD, uuid);
        if (checkTaskLimit.getFirst() == null) {
            logger.error("Критерии возможности выполнения задач задаются в конфигурационных параметрах (параметры асинхронных заданий). За разъяснениями обратитесь к Администратору");
            throw new AsyncTaskException(new ServiceLoggerException(ReportType.CHECK_TASK,
                    logEntryService.save(logger.getEntries()),
                    ReportType.IMPORT_FD.getDescription(),
                    String.format("выбранный файл имеет слишком большой размер (%s байт)!",  checkTaskLimit.getSecond())));
        }
        return checkTaskLimit.getFirst();
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        String uuid = (String)params.get("uuid");
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                manual,
                logger);
        BlobData blobData = blobDataService.get(uuid);

        //Создание временного среза для нф
        dataRowService.createTemporary(formData);
        logger.info("Загрузка данных из файла: \"" + blobData.getName() + "\"");
        //Парсит загруженный в фаловое хранилище xls-файл
        formDataService.importFormData(logger, userInfo,
                formData.getId(), formData.isManual(), blobData.getInputStream(), blobData.getName(), new LockStateLogger() {
                    @Override
                    public void updateState(String state) {
                        lockService.updateState(lock, lockDate, state);
                    }
                });
        // восстанавливаем временный срез, чтобы продолжить редактирование
        dataRowService.createTemporary(formData);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Импорт XLSX-файла";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = false;
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        Integer periodOrder = formData.getPeriodOrder();
        if (periodOrder == null){
            return String.format("Выполнен импорт данных из XLSM файла в экземпляр налоговой формы: Период: \"%s, %s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\"", reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format("Выполнен импорт данных из XLSM файла в экземпляр налоговой формы: Период: \"%s, %s\", Месяц: \"%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\"", reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), Formats.getRussianMonthNameWithTier(formData.getPeriodOrder()), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        }
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = false;
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
        Department department = departmentService.getDepartment(formData.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
        Integer periodOrder = formData.getPeriodOrder();
        if (periodOrder == null){
            return String.format("Не выполнен импорт данных из XLSM файла в экземпляр налоговой формы: Период: \"%s, %s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\". Найдены фатальные ошибки.",
                    reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format("Не выполнен импорт данных из XLSM файла в экземпляр налоговой формы: Период: \"%s, %s\", Месяц: \"%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\". Найдены фатальные ошибки.",
                    reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), Formats.getRussianMonthNameWithTier(formData.getPeriodOrder()), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        }
    }
}
