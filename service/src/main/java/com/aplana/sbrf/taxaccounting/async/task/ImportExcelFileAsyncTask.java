package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Загрузка данных РНУ НДФЛ из Excel-файла в ПНФ
 */
@Component("ImportExcelFileAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ImportExcelFileAsyncTask extends AbstractAsyncTask {

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private DeclarationService declarationService;

    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RefBookAsnuService refBookAsnuService;

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        Map<String, Object> params = taskData.getParams();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        final String blobDataId = (String) params.get("blobDataId");
        long declarationDataId = (Long) params.get("declarationDataId");
        BlobData blobData = blobDataService.get(blobDataId);
        asyncManager.updateState(taskData.getId(), AsyncTaskState.FILES_UPLOADING);
        declarationDataService.importExcel(declarationDataId, blobData, userInfo, logger);
        if(logger.containsLevel(LogLevel.ERROR)) {
            return new BusinessLogicResult(false, null);
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        final String blobDataId = (String) taskData.getParams().get("blobDataId");
        BlobData blobData = blobDataService.get(blobDataId);
        String fileName = blobData.getName();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        ReportPeriod reportPeriod = departmentReportPeriodService.fetchOne(declarationData.getDepartmentReportPeriodId()).getReportPeriod();
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());
        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.getDeclarationTemplateId());
        RefBookAsnu asnu = refBookAsnuService.fetchById(declarationData.getAsnuId());
        return String.format("Загрузка файла \"%s\" завершена: Выполнена загрузка новых данных в налоговую форму: " +
                "№: \"%s\", Период: \"%s, %s\", Подразделение: \"%s\", Вид: \"%s\", АСНУ: \"%s\".",
                fileName, declarationDataId, reportPeriod.getTaxPeriod().getYear(),
                reportPeriod.getName(), department.getName(), declarationTemplate.getName(), asnu.getName());
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        final String fileName = (String) taskData.getParams().get("fileName");
        return "Произошла ошибка при загрузке файла \"" + fileName + "\"";
    }

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        long blobLength = params.containsKey("fileSize") ? (Long) params.get("fileSize") : 0;
        long fileSize = (long) Math.ceil(blobLength / 1024.);
        String msg = String.format("размер файла (%s Кбайт) превышает максимально допустимый (%s Кбайт).", fileSize, "%s");
        return checkTask(fileSize, taskDescription, msg);
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.IMPORT_DECLARATION_EXCEL;
    }

}
