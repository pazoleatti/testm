package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Массовая выгрузка отчетности по формам
 */
@Component("ExportReportsAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExportReportsAsyncTask extends AbstractAsyncTask {

    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private BlobDataService blobDataService;

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.SHORT;
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.EXPORT_REPORTS;
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        Map<String, Object> params = taskData.getParams();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        List<Long> declarationDataIdList = (List<Long>) params.get("declarationDataIds");
        String reportId = declarationDataService.exportReports(declarationDataIdList, userInfo, logger);
        params.put("reportId", reportId);
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, reportId);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        List<Long> declarationDataIdList = (List<Long>) taskData.getParams().get("declarationDataIds");
        boolean allAccepted = true;
        List<DeclarationData> declarations = declarationDataService.get(declarationDataIdList);
        for (DeclarationData declaration : declarations) {
            if (declaration.getState() != State.ACCEPTED) {
                allAccepted = false;
                break;
            }
        }
        BlobData blobData = blobDataService.get((String) taskData.getParams().get("reportId"));
        if (allAccepted) {
            return "Сформирован архив \"" + blobData.getName() + "\" c отчетными формами";
        } else {
            return "Сформирован архив \"" + blobData.getName() + "\" c отчетными формами. Внимание в архиве есть не принятые отчетные формы!";
        }
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        Throwable throwable = (Throwable) taskData.getParams().get("exceptionThrown");
        return "Не выполнена операция \"" + getAsyncTaskType().getDescription() + "\"." + (throwable != null ? " Причина: " + throwable.getMessage() : "");
    }

}