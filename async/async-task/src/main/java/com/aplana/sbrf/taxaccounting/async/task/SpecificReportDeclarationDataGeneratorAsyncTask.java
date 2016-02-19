package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

public abstract class SpecificReportDeclarationDataGeneratorAsyncTask extends AbstractAsyncTask {

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
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.SPECIFIC_REPORT_DEC;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        long declarationDataId = (Long)params.get("declarationDataId");
        String alias = (String)params.get("alias");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Long value = declarationDataService.getValueForCheckLimit(userInfo, declarationDataId, getReportType());
        if (value == null) {
            throw new AsyncTaskException(new ServiceLoggerException("Декларация не сформирована", null));
        }
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(alias);
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias));

        String msg = String.format("xml файл %s имеет слишком большой размер(%s Кбайт)!",  declarationTemplate.getType().getTaxType().getDeclarationShortName(), value);
        return checkTask(getReportType(), value, declarationDataService.getTaskName(ddReportType, declarationTemplate.getType().getTaxType()), msg);
    }

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) {
        long declarationDataId = (Long)params.get("declarationDataId");
        String alias = (String)params.get("alias");

        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(alias);
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias));
        if (declarationData != null) {
            declarationDataService.createSpecificReport(logger, declarationData, ddReportType, userInfo, new LockStateLogger() {
                @Override
                public void updateState(String state) {
                    lockService.updateState(lock, lockDate, state);
                }
            });
        }
        return new TaskStatus(true, null);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Формирование специфичного отчета декларации";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        String alias = (String)params.get("alias");

        long declarationDataId = (Long)params.get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        DeclarationData declaration = declarationDataService.get(declarationDataId, userInfo);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        DeclarationSubreport subreport = declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), alias);
        String str, strCorrPeriod = "";
        if (TaxType.PROPERTY.equals(declarationTemplate.getType().getTaxType()) || TaxType.TRANSPORT.equals(declarationTemplate.getType().getTaxType())) {
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\".", declaration.getTaxOrganCode(), declaration.getKpp());
        } else {
            str = ".";
        }
        if (reportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate());
        }
        return String.format("Сформирован отчет \"%s\": Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s",
                subreport.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), strCorrPeriod, department.getName(),
                declarationTemplate.getType().getName(), str);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        String alias = (String)params.get("alias");

        long declarationDataId = (Long)params.get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        DeclarationData declaration = declarationDataService.get(declarationDataId, userInfo);
        Department department = departmentService.getDepartment(declaration.getDepartmentId());
        DepartmentReportPeriod reportPeriod = departmentReportPeriodService.get(declaration.getDepartmentReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
        DeclarationSubreport subreport = declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), alias);
        String str, strCorrPeriod = "";
        if (TaxType.PROPERTY.equals(declarationTemplate.getType().getTaxType()) || TaxType.TRANSPORT.equals(declarationTemplate.getType().getTaxType())) {
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\".", declaration.getTaxOrganCode(), declaration.getKpp());
        } else {
            str = ".";
        }
        if (reportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.format(reportPeriod.getCorrectionDate());
        }
        return String.format("Произошла непредвиденная ошибка при формировании отчета \"%s\": Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета",
                subreport.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), strCorrPeriod, department.getName(),
                declarationTemplate.getType().getName(), str);
    }
}
