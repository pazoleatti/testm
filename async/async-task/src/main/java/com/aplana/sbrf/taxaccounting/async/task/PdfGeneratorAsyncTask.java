package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCK_DATE;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

public abstract class PdfGeneratorAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DeclarationDataScriptingService scriptingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockService;

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) {
        long declarationDataId = (Long)params.get("declarationDataId");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Pair<BalancingVariants, Long> checkTaskLimit = declarationDataService.checkTaskLimit(userInfo, declarationDataId, ReportType.PDF_DEC);
        if (checkTaskLimit == null) {
            throw new ServiceException("Декларация не сформирована");
        } else if (checkTaskLimit.getFirst() == null) {
            Logger logger = new Logger();
            DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
            logger.error("Критерий возможности формирования формы предварительного просмотра %s задается в конфигурационных параметрах. За разъяснениями обратитесь к Администратору", declarationTemplate.getType().getTaxType().getDeclarationShortName());
            throw new ServiceLoggerException(ReportType.CHECK_TASK,
                    logEntryService.save(logger.getEntries()),
                    String.format(ReportType.EXCEL_DEC.getDescription(), declarationTemplate.getType().getTaxType().getDeclarationShortName()),
                    String.format("xml файл %s имеет слишком большой размер(%s байт)!",  declarationTemplate.getType().getTaxType().getDeclarationShortName(), checkTaskLimit.getSecond()));
        }
        return checkTaskLimit.getFirst();
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
            Map<String, Object> scriptParams = new HashMap<String, Object>();
            ScriptProcessedModel scriptProcessedModel = new ScriptProcessedModel();
            scriptProcessedModel.setProcessedByScript(false);
            scriptParams.put("scriptProcessedModel", scriptProcessedModel);
            scriptParams.put("needPdf", true);
            scriptParams.put("needXlsx", false);
            scriptingService.executeScript(userInfo, declarationData, FormDataEvent.REPORT, logger, scriptParams);
            if (!scriptProcessedModel.isProcessedByScript()) {
                declarationDataService.setPdfDataBlobs(logger, declarationData, userInfo, new LockStateLogger() {
                    @Override
                    public void updateState(String state) {
                        lockService.updateState(lock, lockDate, state);
                    }
                });
            }
        }
    }

    @Override
    protected String getAsyncTaskName() {
        return "Формирование pdf-файла";
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
        return String.format("Сформирован %s отчет декларации: Период: \"%s, %s\", Подразделение: \"%s\", Вид: \"%s\"%s",
                ReportType.PDF_DEC.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(),
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
            str = String.format(", Налоговый орган: \"%s\", КПП: \"%s\".", declaration.getTaxOrganCode(), declaration.getKpp());
        } else {
            str = ".";
        }
        return String.format("Произошла непредвиденная ошибка при формировании %s отчета декларации: Период: \"%s, %s\", Подразделение: \"%s\", Вид: \"%s\"%s Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета",
                ReportType.PDF_DEC.getName(), reportPeriod.getReportPeriod().getTaxPeriod().getYear(), reportPeriod.getReportPeriod().getName(), department.getName(),
                declarationTemplate.getType().getName(), str);
    }
}
