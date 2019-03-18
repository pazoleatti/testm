package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component("CreateNotificationsLogsAsyncTask")
@SuppressWarnings("unchecked")
public class CreateNotificationsLogsAsyncTask extends AbstractAsyncTask {

    private final NotificationService notificationService;
    private final BlobDataService blobDataService;

    public CreateNotificationsLogsAsyncTask(NotificationService notificationService, BlobDataService blobDataService) {
        this.notificationService = notificationService;
        this.blobDataService = blobDataService;
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        List<Long> notificationIds = (List<Long>) taskData.getParams().get("notificationIds");
        String fileUuid = notificationService.createLogsReport(notificationIds);
        if (logger.containsLevel(LogLevel.ERROR) || fileUuid == null) {
            return new BusinessLogicResult(false, null);
        }
        BlobData blobData = blobDataService.get(fileUuid);
        String fileName = blobData.getName();
        taskData.getParams().put("fileName", fileName);
        return new BusinessLogicResult(true, NotificationType.REF_BOOK_REPORT, fileUuid);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        Map<String, Object> params = taskData.getParams();
        if (params.containsKey("fileName")) {
            return "Сформирован архив: \"" + params.get("fileName") + "\"";
        } else {
            return "Сформирован архив протоколов оповещений";
        }
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        Map<String, Object> params = taskData.getParams();
        if (params.containsKey("exceptionThrown")) {
            Exception e = (Exception) params.get("exceptionThrown");
            return "Не выполнена операция \"Выгрузка протоколов оповещений\". Причина: " + e.getMessage();
        } else {
            return "Не выполнена операция \"Выгрузка протоколов оповещений\"";
        }
    }

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return AsyncQueue.SHORT;
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.CREATE_NOTIFICATIONS_LOGS;
    }

    @Override
    @Deprecated
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return null;
    }
}
