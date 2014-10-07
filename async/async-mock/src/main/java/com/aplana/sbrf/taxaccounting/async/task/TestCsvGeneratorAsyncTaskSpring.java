package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

/**
 * Спринговая реализация таска "Генерация xlsm-файл" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("TestCSVGeneratorAsyncTaskSpring")
@Transactional
public class TestCsvGeneratorAsyncTaskSpring extends AbstractAsyncTask {

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

    @Override
    protected void executeBusinessLogic(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long formDataId = (Long)params.get("formDataId");
        boolean manual = (Boolean)params.get("manual");
        boolean isShowChecked = (Boolean)params.get("isShowChecked");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        formDataAccessService.canRead(userInfo, formDataId);
        String uuid = printingService.generateCSV(userInfo, formDataId, manual, isShowChecked);
        reportService.create(formDataId, uuid, ReportType.CSV, isShowChecked, manual, false);
    }

    protected String getAsyncTaskName() {
        return "Генерация csv-файла";
    }

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
        if (periodOrder == null){
            return String.format("Сформирован %s отчет налоговой формы: Период: \"%s, %s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\".", ReportType.CSV.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        } else {
            return String.format("Сформирован %s отчет налоговой формы: Период: \"%s, %s\", Месяц: \"%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\".", ReportType.CSV.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), Formats.getRussianMonthNameWithTier(formData.getPeriodOrder()), department.getName(), formData.getKind().getName(), formData.getFormType().getName(), manual ? "ручного ввода" : "автоматическая");
        }
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        return null;
    }
}
