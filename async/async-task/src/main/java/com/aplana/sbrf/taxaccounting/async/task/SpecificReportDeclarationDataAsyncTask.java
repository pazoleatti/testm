package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

public abstract class SpecificReportDeclarationDataAsyncTask extends AbstractDeclarationAsyncTask {

    private static final String SUCCESS = "Сформирован отчет \"%s\": %s";
    private static final String FAIL = "Произошла %sошибка при формировании отчета \"%s\": %s. Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета";

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

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
                return new TaskStatus(true, NotificationType.REF_BOOK_REPORT, uuid);
            }
        }
        return new TaskStatus(true, null);
    }

    @Override
    protected DeclarationDataReportType getDeclarationDataReportType(Map<String, Object> params) {
        String alias = (String)params.get("alias");
        DeclarationData declarationData = getDeclaration(params);
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(alias);
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias));
        return ddReportType;
    }

    @Override
    protected String getAsyncTaskName() {
        return "Формирование специфичного отчета налоговой формы";
    }

    @Override
    protected String getAdditionalString(DeclarationData declaration, Map<String, Object> params) {
        String alias = (String)params.get("alias");
        DeclarationSubreport subreport = declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), alias);
        StringBuilder strSubreportParamValues = new StringBuilder(", ");
        if (!subreport.getDeclarationSubreportParams().isEmpty()) {
            Map<String, Object> subreportParamValues = (Map<String, Object>)params.get("subreportParamValues");
            for(DeclarationSubreportParam declarationSubreportParam: subreport.getDeclarationSubreportParams()) {
                strSubreportParamValues.append(declarationSubreportParam.getName()).append(": ");
                Object value = subreportParamValues.get(declarationSubreportParam.getAlias());
                strSubreportParamValues.append("\"");
                if (value != null) {
                    switch (declarationSubreportParam.getType()) {
                        case STRING:
                            strSubreportParamValues.append(value.toString());
                            break;
                        case NUMBER:
                            strSubreportParamValues.append(value.toString());
                            break;
                        case DATE:
                            strSubreportParamValues.append(SDF_DD_MM_YYYY.get().format((Date) value));
                            break;
                        case REFBOOK:
                            String strVal = refBookHelper.dereferenceValue((Long) value, declarationSubreportParam.getRefBookAttributeId());
                            strSubreportParamValues.append(strVal);
                            break;
                    }
                }
                strSubreportParamValues.append("\"");
                strSubreportParamValues.append(", ");
            }
        }
        strSubreportParamValues = strSubreportParamValues.delete(strSubreportParamValues.length() - 2, strSubreportParamValues.length());
        String str = super.getAdditionalString(declaration, params);
        if (str.isEmpty()) {
            return strSubreportParamValues.toString();
        } else if (strSubreportParamValues.length() > 0) {
            return str + strSubreportParamValues.toString();
        } else {
            return str;
        }
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
        return getMessage(params, false, unexpected);
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return getMessage(params, true, false);
    }

    private String getMessage(Map<String, Object> params, boolean isSuccess, boolean unexpected) {
        String alias = (String)params.get("alias");
        DeclarationData declaration = getDeclaration(params);
        DeclarationSubreport subreport = declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), alias);

        String template = isSuccess ? SUCCESS : FAIL;
        if (isSuccess) {
            return String.format(template,
                    subreport.getName(),
                    getDeclarationDescription(params));
        } else {
            return String.format(template,
                    unexpected ? "непредвиденная " : "",
                    subreport.getName(),
                    getDeclarationDescription(params));
        }
    }
}
