package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.UploadTransportDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Загрузка ТФ (ТФ РНУ НДФЛ, файла ответа 6-НДФЛ, файла ответа 2-НДФЛ)
 */
@Component("LoadTransportFileAsyncTask")
public class LoadTransportFileAsyncTask extends AbstractAsyncTask {

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private UploadTransportDataService uploadTransportDataService;

    private String msg;

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        Map<String, Object> params = taskData.getParams();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        final String blobDataId = (String) params.get("blobDataId");
        final TransportFileType fileType = (TransportFileType) params.get("fileType");
        BlobData blobData = blobDataService.get(blobDataId);
        asyncManager.updateState(taskData.getId(), AsyncTaskState.FILES_UPLOADING);
        msg = uploadTransportDataService.processTransportFileUploading(logger, userInfo, fileType, blobData.getName(), blobData.getInputStream(), taskData.getId());
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        final String blobDataId = (String) taskData.getParams().get("blobDataId");
        BlobData blobData = blobDataService.get(blobDataId);
        String fileName= blobData.getName();
        return "Загрузка файла \"" + fileName + "\" завершена" + (msg.isEmpty() ? "" : (": " + msg));
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        final String blobDataId = (String) taskData.getParams().get("blobDataId");
        BlobData blobData = blobDataService.get(blobDataId);
        String fileName = blobData.getName();
        return "Произошла непредвиденная ошибка при загрузке файла \"" + fileName + "\"";
    }

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        long blobLength = blobDataService.getLength((String) params.get("blobDataId"));
        long fileSize = (long) Math.ceil(blobLength / 1024.);
        String msg = String.format("Размер файла(%s) превышает максимально допустимый(%s)!", fileSize, "%s");
        return checkTask(fileSize, taskDescription, msg);
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.LOAD_TRANSPORT_FILE;
    }
}
