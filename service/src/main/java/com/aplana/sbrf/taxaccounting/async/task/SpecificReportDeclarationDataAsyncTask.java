package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;


@Component("SpecificReportDeclarationDataAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SpecificReportDeclarationDataAsyncTask extends AbstractDeclarationAsyncTask {

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
        DeclarationReportType ddReportType = DeclarationReportType.createSpecificReport();
        ddReportType.setSubreport(declarationTemplateService.getSubreportByAlias(declarationData.getDeclarationTemplateId(), alias));
        params.put("ddReportType", ddReportType);

        Map<String, Object> subreportParamValues = (Map<String, Object>) params.get("subreportParamValues");
        Map<String, String> viewParamValues = Collections.EMPTY_MAP;
        DataRow<Cell> selectedRecord = null;
        if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
            viewParamValues = (Map<String, String>) params.get("viewParamValues");
            if (params.containsKey("selectedRecord")) {
                selectedRecord = (DataRow<Cell>) params.get("selectedRecord");
            }
        }

        if (declarationData != null) {
            SpecificReportContext specificReportContext = SpecificReportContext.builder()
                    .logger(logger)
                    .declarationData(declarationData)
                    .ddReportType(ddReportType)
                    .subreportParamValues(subreportParamValues)
                    .viewParamValues(viewParamValues)
                    .selectedRecord(selectedRecord)
                    .userInfo(userInfo)
                    .searchFilter((RnuNdflAllPersonsReportFilter) params.get("searchFilter"))
                    .selectedRows((RnuNdflAllPersonsReportSelectedRows) params.get("selectedRows"))
                    .build();
            String uuid = declarationDataService.createSpecificReport(specificReportContext, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            });
            if (!ddReportType.getSubreport().getDeclarationSubreportParams().isEmpty()) {
                return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
            } else {
                reportService.attachReportToDeclaration(declarationData.getId(), uuid, ddReportType);
                return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
            }
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getAdditionalString(DeclarationData declaration, Map<String, Object> params) {
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
        String errorsText = (String) taskData.getParams().get("errorsText");
        // Для ожидаемых исключений выводим в оповещение текст из errorsText, сфомрмированный из логов
        if (!unexpected && !errorsText.isEmpty()) {
            return errorsText;
        } else {
            String reportName = getReportName(taskData);
            String declarationDescription = getDeclarationDescription(taskData);
            return String.format("Произошла непредвиденная ошибка при формировании отчета \"%s\": %s. Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета",
                    reportName,
                    declarationDescription);
        }
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        DeclarationReportType ddReportType = (DeclarationReportType) taskData.getParams().get("ddReportType");
        String reportName = getReportName(taskData);
        String declarationDescription = getDeclarationDescription(taskData);
        if (ddReportType.getSubreport().getAlias().equals(SubreportAliasConstants.RNU_NDFL_2_6_DATA_XLSX_REPORT) ||
                ddReportType.getSubreport().getAlias().equals(SubreportAliasConstants.RNU_NDFL_2_6_DATA_TXT_REPORT)) {
            return String.format("Сформирован %s: %s",
                    reportName,
                    declarationDescription);
        } else {
            return String.format("Сформирован отчет \"%s\": %s",
                    reportName,
                    declarationDescription);
        }
    }

    /**
     * Название формируемого отчёта.
     *
     * @param taskData данные асинхронной задачи формирования отчёта
     * @return название формируемого отчёта
     */
    private String getReportName(AsyncTaskData taskData) {
        String alias = (String) taskData.getParams().get("alias");
        DeclarationData declaration = getDeclaration(taskData.getUserId(), taskData.getParams());
        int declarationTemplateId = declaration.getDeclarationTemplateId();
        DeclarationSubreport report = declarationTemplateService.getSubreportByAlias(declarationTemplateId, alias);
        return report.getName();
    }

    /**
     * Текстовое представление параметров НФ.
     *
     * @param taskData данные асинхронной задачи формирования отчёта
     * @return текст с параметрами НФ
     */
    private String getDeclarationDescription(AsyncTaskData taskData) {
        return getDeclarationDescription(taskData.getUserId(), taskData.getParams());
    }

}
