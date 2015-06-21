package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

public abstract class CheckDeclarationAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockService;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected ReportType getReportType() {
        return ReportType.CHECK_DEC;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) throws AsyncTaskException {
        long declarationDataId = (Long)params.get("declarationDataId");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Long value = declarationDataService.getValueForCheckLimit(userInfo, declarationDataId, getReportType());
        if (value == null) {
            throw new AsyncTaskException(new ServiceLoggerException("Декларация не сформирована", null));
        }
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        String msg = String.format("xml файл %s имеет слишком большой размер(%s Кбайт)!",  declarationTemplate.getType().getTaxType().getDeclarationShortName(), value);
        return checkTask(getReportType(), value, String.format(getReportType().getDescription(), declarationTemplate.getType().getTaxType().getDeclarationShortName()), msg);
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        long declarationDataId = (Long)params.get("declarationDataId");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            declarationDataService.check(logger, declarationDataId, userInfo, new LockStateLogger() {
                @Override
                public void updateState(String state) {
                    lockService.updateState(lock, lockDate, state);
                }
            });
        }
    }

    @Override
    protected String getAsyncTaskName() {
        return "Проверить";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long declarationDataId = (Long)params.get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        DeclarationData declaration = declarationDataService.get(declarationDataId, userInfo);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        String str;
        if (TaxType.PROPERTY.equals(declarationTemplate.getType().getTaxType()) || TaxType.TRANSPORT.equals(declarationTemplate.getType().getTaxType())) {
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\".", declaration.getTaxOrganCode(), declaration.getKpp());
        } else {
            str = ".";
        }
        return String.format("Выполнена проверка %s: Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s",
                declarationTemplate.getType().getTaxType().getDeclarationShortName(),
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(),
                reportPeriod.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                        formatter.format(reportPeriod.getCorrectionDate())) : "",
                department.getName(),
                declarationTemplate.getType().getName(), str);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        long declarationDataId = (Long)params.get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        DeclarationData declaration = declarationDataService.get(declarationDataId, userInfo);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        String str;
        if (TaxType.PROPERTY.equals(declarationTemplate.getType().getTaxType()) || TaxType.TRANSPORT.equals(declarationTemplate.getType().getTaxType())) {
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\"", declaration.getTaxOrganCode(), declaration.getKpp());
        } else {
            str = "";
        }
        return String.format("Выполнена проверка %s: Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s. Найдены фатальные ошибки.",
                declarationTemplate.getType().getTaxType().getDeclarationShortName(),
                reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(),
                reportPeriod.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s",
                        formatter.format(reportPeriod.getCorrectionDate())) : "",
                department.getName(),
                declarationTemplate.getType().getName(), str);
    }
}
