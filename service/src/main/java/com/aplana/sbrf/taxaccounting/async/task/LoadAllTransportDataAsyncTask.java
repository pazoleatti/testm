package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LoadDeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;


/**
 * Реализация таска "Загрузка файлов"
 */
@Component("LoadAllTransportDataAsyncTask")
public class LoadAllTransportDataAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private LoadDeclarationDataService loadDeclarationDataService;

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.LOAD_ALL_TF;
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo userInfo, Map<String, Object> params, Logger logger) throws AsyncTaskException {
        if (params.containsKey("blobDataId")) {
            long blobLength = blobDataService.getLength((String) params.get("blobDataId"));
            long fileSize = (long) Math.ceil(blobLength / 1024.);
            String msg = String.format("Размер файла(%s) превышает максимально допустимый(%s)!", fileSize, "%s");
            return checkTask(fileSize, taskDescription, msg);
        } else {
            return AsyncQueue.LONG;
        }
    }

    private String msg;

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) {
        Map<String, Object> params = taskData.getParams();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));

        if (params.containsKey("blobDataId")) {
            final String blobDataId = (String) params.get("blobDataId");
            BlobData blobData = blobDataService.get(blobDataId);
            asyncManager.updateState(taskData.getId(), AsyncTaskState.FILES_UPLOADING);
            msg = loadDeclarationDataService.uploadFile(logger, userInfo, blobData.getName(), blobData.getInputStream(), taskData.getId());
        } else {
            String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
            lockDataService.lock(key, userInfo.getUser().getId(),
                    DescriptionTemplate.CONFIGURATION_PARAMS.getText());
            try {
                logger.info("Номер загрузки: %s", taskData.getId());
                //Импорт справочника ФИАС
                loadRefBookDataService.importRefBookFias(userInfo, null, logger, taskData.getId());
            } finally {
                lockDataService.unlock(key, userInfo.getUser().getId());
            }
        }
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        if (taskData.getParams().containsKey("blobDataId")) {
            final String blobDataId = (String) taskData.getParams().get("blobDataId");
            BlobData blobData = blobDataService.get(blobDataId);
            String fileName;
            if (blobData != null) {
                fileName = blobData.getName();
            } else {
                fileName = blobDataId;
            }
            return "Загрузка файла \"" + fileName + "\" завершена" + (msg.isEmpty() ? "" : (": " + msg));
        } else {
            return "Загрузка файлов завершена";
        }
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        if (taskData.getParams().containsKey("blobDataId")) {
            final String blobDataId = (String) taskData.getParams().get("blobDataId");
            BlobData blobData = blobDataService.get(blobDataId);
            String fileName;
            if (blobData != null) {
                fileName = blobData.getName();
            } else {
                fileName = blobDataId;
            }
            return "Произошла непредвиденная ошибка при загрузке файла \"" + fileName + "\"";
        } else {
            return "Произошла непредвиденная ошибка при загрузке файлов";
        }
    }

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return getAsyncTaskType().getDescription();
    }
}
