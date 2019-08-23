package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.UploadTransportDataResult;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.UploadTransportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Загрузка ТФ (ТФ РНУ НДФЛ, файла ответа 6-НДФЛ, файла ответа 2-НДФЛ)
 */
@Component("LoadTransportFileAsyncTask")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LoadTransportFileAsyncTask extends AbstractAsyncTask {

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private UploadTransportDataService uploadTransportDataService;

    private String notificationMsg = "";

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        Map<String, Object> params = taskData.getParams();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        userInfo.setIp((String) params.get("userIP"));
        final String blobDataId = (String) params.get("blobDataId");
        BlobData blobData = blobDataService.get(blobDataId);
        asyncManager.updateState(taskData.getId(), AsyncTaskState.FILES_UPLOADING);
        UploadTransportDataResult uploadTransportDataResult = uploadTransportDataService.processTransportFileUploading(
                logger, userInfo, blobData.getName(), blobData.getInputStream(), false);
        notificationMsg = uploadTransportDataResult.getNotificationMessage();
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        String fileName = getFileName(taskData);
        final String archiveName = (String) taskData.getParams().get("archiveName");
        String archive = archiveName == null ? "" : String.format(" (из архива \"%s\")", archiveName);
        return "Загрузка файла \"" + fileName + "\"" + archive + " завершена" + (notificationMsg.isEmpty() ? "" : (": " + notificationMsg));
    }


    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {

        String fileName = getFileName(taskData);
        final String archiveName = (String) taskData.getParams().get("archiveName");
        String archive = archiveName == null ? "" : String.format(" (из архива \"%s\")", archiveName);

        if (unexpected) {
            return "Произошла непредвиденная ошибка при загрузке файла \"" + fileName + "\"" + archive;
        } else {
            // Для ожидаемых ошибок добавляем описание
            Throwable e = (Throwable) taskData.getParams().get("exceptionThrown");
            String exceptionMessage = "";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                exceptionMessage += e.getMessage();
            }
            return "Загрузка файла \"" + fileName + "\"" + archive + " завершена с ошибкой. " + exceptionMessage;
        }
    }

    /**
     * Извлечение имени файла из данных о выполнении загрузки файла
     *
     * @param taskData данные о выполнении загрузки файла
     * @return имя загружаемого файла
     */
    private String getFileName(AsyncTaskData taskData) {
        final String blobDataId = (String) taskData.getParams().get("blobDataId");
        BlobData blobData = blobDataService.get(blobDataId);
        return blobData.getName();
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
        return AsyncTaskType.LOAD_TRANSPORT_FILE;
    }

}
