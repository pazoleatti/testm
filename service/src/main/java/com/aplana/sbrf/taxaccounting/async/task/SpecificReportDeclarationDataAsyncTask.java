package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;


@Component("SpecificReportDeclarationDataAsyncTask")
public class SpecificReportDeclarationDataAsyncTask extends AbstractDeclarationAsyncTask {

    private static final String SUCCESS = "Сформирован отчет \"%s\": %s";
    private static final String FAIL = "Произошла %sошибка при формировании отчета \"%s\": %s. Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета";

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private ReportService reportService;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.SPECIFIC_REPORT_DEC;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        long declarationDataId = (Long) params.get("declarationDataId");
        String alias = (String) params.get("alias");

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(alias);
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias));

        Map<String, Object> subreportParamValues = null;
        Map<String, String> viewParamValues = Collections.EMPTY_MAP;
        DataRow<Cell> selectedRecord = null;
        if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
            subreportParamValues = (Map<String, Object>) params.get("subreportParamValues");
            viewParamValues = (Map<String, String>) params.get("viewParamValues");
            if (params.containsKey("selectedRecord")) {
                selectedRecord = (DataRow<Cell>) params.get("selectedRecord");
            }
        }

        if (declarationData != null) {
            String uuid = declarationDataService.createSpecificReport(logger, declarationData, ddReportType, subreportParamValues, viewParamValues, selectedRecord, userInfo, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            });
            if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
                return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
            } else {
                reportService.createDec(declarationData.getId(), uuid, ddReportType);
                return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
            }
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getAdditionalString(DeclarationData declaration, Map<String, Object> params) {
        String alias = (String) params.get("alias");
        DeclarationSubreport subreport = declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), alias);
        StringBuilder strSubreportParamValues = new StringBuilder(", ");

        Map<String, String> subreportParamValues = (Map<String, String>) params.get("viewParamValues");
        for (Map.Entry<String, String> entry : subreportParamValues.entrySet()) {
            strSubreportParamValues.append(entry.getKey()).append(": ");
            strSubreportParamValues.append("\"");
            strSubreportParamValues.append(entry.getValue());
            strSubreportParamValues.append("\"");
            strSubreportParamValues.append(", ");

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
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return getMessage(taskData, false, unexpected);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return getMessage(taskData, true, false);
    }

    private String getMessage(AsyncTaskData taskData, boolean isSuccess, boolean unexpected) {
        String alias = (String) taskData.getParams().get("alias");
        DeclarationData declaration = getDeclaration(taskData.getUserId(), taskData.getParams());
        DeclarationSubreport subreport = declarationTemplateService.getSubreportByAlias(declaration.getDeclarationTemplateId(), alias);

        String template = isSuccess ? SUCCESS : FAIL;
        if (isSuccess) {
            return String.format(template,
                    subreport.getName(),
                    getDeclarationDescription(taskData.getUserId(), taskData.getParams()));
        } else {
            return String.format(template,
                    unexpected ? "непредвиденная " : "",
                    subreport.getName(),
                    getDeclarationDescription(taskData.getUserId(), taskData.getParams()));
        }
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        String alias = (String) params.get("alias");
        DeclarationData declarationData = getDeclaration(userInfo, params);
        DeclarationDataReportType ddReportType = DeclarationDataReportType.getDDReportTypeByName(alias);
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias));

        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(ddReportType.getReportType().getDescription(), ddReportType.getSubreport().getName(), declarationDataService.getDeclarationFullName(declarationDataId, ddReportType));
    }
}
