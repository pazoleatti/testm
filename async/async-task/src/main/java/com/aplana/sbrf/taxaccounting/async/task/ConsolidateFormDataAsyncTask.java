package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class ConsolidateFormDataAsyncTask extends AbstractAsyncTask {

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

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        Logger logger = new Logger();
        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                false,
                logger);
        try {
            formDataService.checkCompose(formData, userInfo, logger);
        } catch (ServiceException e) {
            throw new ServiceLoggerException(ReportType.CHECK_TASK,
                    logEntryService.save(logger.getEntries()),
                    String.format(ReportType.CONSOLIDATE_FD.getDescription(), formData.getFormType().getTaxType().getTaxText()),
                    e.getMessage());
        }
        Pair<BalancingVariants, Long> checkTaskLimit = formDataService.checkTaskLimit(userInfo, formData, ReportType.CONSOLIDATE_FD);
        return checkTaskLimit.getFirst();
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        checkTaskLimit(params);
        FormData formData = formDataService.getFormData(
                userInfo,
                formDataId,
                false,
                logger);
        formDataService.compose(formData, userInfo, logger);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Конслолидация НФ";
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
            return String.format("Выполнена консолидация в налоговую форму: Период: \"%s, %s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\"", reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format("Выполнена консолидация в налоговую форму: Период: \"%s, %s\", Месяц: \"%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\"", reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), Formats.getRussianMonthNameWithTier(formData.getPeriodOrder()), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
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
            return String.format("Выполнена консолидация в налоговую форму: Период: \"%s, %s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\". Найдены фатальные ошибки.",
                    reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format("Выполнена консолидация в налоговую форму: Период: \"%s, %s\", Месяц: \"%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\". Найдены фатальные ошибки.",
                    reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), Formats.getRussianMonthNameWithTier(formData.getPeriodOrder()), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        }
    }
}
