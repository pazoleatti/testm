package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component("PdfGeneratorAsyncTask")
public class PdfGeneratorAsyncTask extends AbstractDeclarationAsyncTask {

    private static final String SUCCESS = "Сформирован %s отчет налоговой формы: %s";
    private static final String FAIL = "Произошла непредвиденная ошибка при формировании %s отчета налоговой формы: %s Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета";

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.PDF_DEC;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            declarationDataService.setPdfDataBlobs(logger, declarationData, userInfo, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            });
        }
        return new BusinessLogicResult(true, null);
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
        String template = isSuccess ? SUCCESS : FAIL;
        return String.format(template,
                getAsyncTaskType().getName(),
                getDeclarationDescription(taskData.getUserId(), taskData.getParams()));
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(getAsyncTaskType().getDescription(),
                declarationDataService.getDeclarationFullName(declarationDataId, getDeclarationDataReportType(userInfo, params)));
    }

}
