package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component("XlsxGeneratorAsyncTask")
public class XlsxGeneratorAsyncTask extends AbstractDeclarationAsyncTask {

    private static final String SUCCESS = "Сформирован %s отчет налоговой формы: %s";
    private static final String FAIL = "Произошла непредвиденная ошибка при формировании %s отчета налоговой формы: %s. Для запуска процедуры формирования необходимо повторно инициировать формирование данного отчета";

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private ReportService reportService;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.EXCEL_DEC;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(final AsyncTaskData taskData, Logger logger) {
        long declarationDataId = (Long) taskData.getParams().get("declarationDataId");
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            String uuid = declarationDataService.setXlsxDataBlobs(logger, declarationData, userInfo, new LockStateLogger() {
                @Override
                public void updateState(AsyncTaskState state) {
                    asyncManager.updateState(taskData.getId(), state);
                }
            });
            reportService.attachReportToDeclaration(declarationData.getId(), uuid, DeclarationDataReportType.EXCEL_DEC);
            return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, uuid);
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return getMessage(taskData, false);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return getMessage(taskData, true);
    }

    private String getMessage(AsyncTaskData taskData, boolean isSuccess) {
        String template = isSuccess ? SUCCESS : FAIL;
        return String.format(template,
                getAsyncTaskType().getName(),
                getDeclarationDescription(taskData.getUserId(), taskData.getParams()));
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        long declarationDataId = (Long) params.get("declarationDataId");
        return String.format(getAsyncTaskType().getDescription(),
                declarationDataService.getDeclarationFullName(declarationDataId, getDeclarationDataReportType(userInfo, params)));
    }

    @Override
    public LockData lockObject(String lockKey, TAUserInfo user, Map<String, Object> params) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean checkLocks(Map<String, Object> params, Logger logger) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
