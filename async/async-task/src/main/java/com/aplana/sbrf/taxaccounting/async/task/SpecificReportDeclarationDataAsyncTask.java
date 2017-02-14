package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

public abstract class SpecificReportDeclarationDataAsyncTask extends AbstractAsyncTask {

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

    @Autowired
    private ReportService reportService;

    @Autowired
    private RefBookHelper refBookHelper;

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

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(alias);
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias));

        Long value = declarationDataService.getValueForCheckLimit(userInfo, declarationDataId, ddReportType);
        if (value == null) {
            throw new AsyncTaskException(new ServiceLoggerException("Декларация не сформирована", null));
        }
        String msg = String.format("количество ячеек форм-источников(%s) превышает максимально допустимое(%s)!",  value, "%s");
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

        Map<String, Object> subreportParamValues = null;
        DataRow<Cell> selectedRecord = null;
        if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
            subreportParamValues = (Map<String, Object>)params.get("subreportParamValues");
            if (params.containsKey("selectedRecord")) {
                selectedRecord = (DataRow<Cell>)params.get("selectedRecord");
            }
        }

        if (declarationData != null) {
            String uuid = declarationDataService.createSpecificReport(logger, declarationData, ddReportType, subreportParamValues, selectedRecord, userInfo, new LockStateLogger() {
                @Override
                public void updateState(String state) {
                    lockService.updateState(lock, lockDate, state);
                }
            });
            if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
                return new TaskStatus(true, NotificationType.REF_BOOK_REPORT, uuid);
            } else {
                reportService.createDec(declarationData.getId(), uuid, ddReportType);
            }
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
        StringBuilder strSubreportParamValues = new StringBuilder(", ");
        if (!subreport.getDeclarationSubreportParams().isEmpty()) {
            Map<String, Object> subreportParamValues = (Map<String, Object>)params.get("subreportParamValues");
            for(DeclarationSubreportParam declarationSubreportParam: subreport.getDeclarationSubreportParams()) {
                strSubreportParamValues.append(declarationSubreportParam.getName()).append(": ");
                Object value = subreportParamValues.get(declarationSubreportParam.getAlias());
                strSubreportParamValues.append("\"");
                switch (declarationSubreportParam.getType()) {
                    case STRING:
                        strSubreportParamValues.append(value!= null?value.toString():"");
                        break;
                    case NUMBER:
                        strSubreportParamValues.append(value!= null?value.toString():"");
                        break;
                    case DATE:
                        strSubreportParamValues.append(value!= null?SDF_DD_MM_YYYY.get().format((Date)value):"");
                        break;
                    case REFBOOK:
                        String strVal = "";
                        if (value != null) {
                            strVal = refBookHelper.dereferenceValue((Long)value, declarationSubreportParam.getRefBookAttributeId());
                        }
                        strSubreportParamValues.append(strVal);
                        break;
                }
                strSubreportParamValues.append("\"");
                strSubreportParamValues.append(", ");
            }
            strSubreportParamValues = strSubreportParamValues.delete(strSubreportParamValues.length() - 2, strSubreportParamValues.length());
        }
        if (TaxType.PROPERTY.equals(declarationTemplate.getType().getTaxType()) || TaxType.TRANSPORT.equals(declarationTemplate.getType().getTaxType())) {
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\", %s.", declaration.getTaxOrganCode(), declaration.getKpp(), strSubreportParamValues.toString());
        } else {
            str = strSubreportParamValues.toString()+".";
        }
        if (reportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.get().format(reportPeriod.getCorrectionDate());
        }
        return String.format("Сформирован отчет \"%s\": Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s",
                subreport.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), strCorrPeriod, department.getName(),
                declarationTemplate.getType().getName(), str);
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
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
        StringBuilder strSubreportParamValues = new StringBuilder(", ");
        if (!subreport.getDeclarationSubreportParams().isEmpty()) {
            Map<String, Object> subreportParamValues = (Map<String, Object>)params.get("subreportParamValues");
            for(DeclarationSubreportParam declarationSubreportParam: subreport.getDeclarationSubreportParams()) {
                strSubreportParamValues.append(declarationSubreportParam.getName()).append(": ");
                Object value = subreportParamValues.get(declarationSubreportParam.getAlias());
                strSubreportParamValues.append("\"");
                switch (declarationSubreportParam.getType()) {
                    case STRING:
                        strSubreportParamValues.append(value.toString());
                        break;
                    case NUMBER:
                        strSubreportParamValues.append(value.toString());
                        break;
                    case DATE:
                        strSubreportParamValues.append(SDF_DD_MM_YYYY.get().format((Date)value));
                        break;
                    case REFBOOK:
                        String strVal = "";
                        if (value != null) {
                            strVal = refBookHelper.dereferenceValue((Long)value, declarationSubreportParam.getRefBookAttributeId());
                        }
                        strSubreportParamValues.append(strVal);
                        break;
                }
                strSubreportParamValues.append("\"");
                strSubreportParamValues.append(", ");
            }
            strSubreportParamValues = strSubreportParamValues.delete(strSubreportParamValues.length() - 2, strSubreportParamValues.length());
        }
        if (TaxType.PROPERTY.equals(declarationTemplate.getType().getTaxType()) || TaxType.TRANSPORT.equals(declarationTemplate.getType().getTaxType())) {
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\"%s.", declaration.getTaxOrganCode(), declaration.getKpp(), strSubreportParamValues.toString());
        } else {
            str = strSubreportParamValues.toString()+".";
        }
        if (reportPeriod.getCorrectionDate() != null) {
            strCorrPeriod = ", с датой сдачи корректировки " + SDF_DD_MM_YYYY.get().format(reportPeriod.getCorrectionDate());
        }
        return String.format("Произошла %sошибка при формировании отчета \"%s\": Период: \"%s, %s%s\", Подразделение: \"%s\", Вид: \"%s\"%s Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета",
                unexpected?"непредвиденная ":"", subreport.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), strCorrPeriod, department.getName(),
                declarationTemplate.getType().getName(), str);
    }
}
