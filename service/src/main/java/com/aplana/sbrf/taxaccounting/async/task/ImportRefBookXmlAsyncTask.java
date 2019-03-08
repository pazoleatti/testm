package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Загрузка xml в справочники
 */
@Component("ImportRefBookXmlAsyncTask")
public class ImportRefBookXmlAsyncTask extends AbstractAsyncTask {

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private CommonRefBookService commonRefBookService;

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return String.format(getAsyncTaskType().getDescription(), params.get("refBookName"));
    }

    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException {
        Map<String, Object> params = taskData.getParams();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        final String blobDataId = (String) params.get("blobDataId");
        long refBookId = (Long)params.get("refBookId");
        BlobData blobData = blobDataService.get(blobDataId);
        asyncManager.updateState(taskData.getId(), AsyncTaskState.FILES_UPLOADING);
        loadRefBookDataService.importXml(refBookId, blobData, userInfo, logger);
        return new BusinessLogicResult(true, null);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        long refBookId = (Long) taskData.getParams().get("refBookId");
        final String blobDataId = (String) taskData.getParams().get("blobDataId");
        final String archiveName = (String) taskData.getParams().get("archiveName");
        BlobData blobData = blobDataService.get(blobDataId);
        String fileName = blobData.getName();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        String archive = archiveName == null ? "" : String.format(" (из архива \"%s\")", archiveName);
        return String.format("Загрузка файла \"%s\"%s в справочник \"%s\" завершена", fileName, archive, commonRefBookService.get(refBookId).getName());
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        final String blobDataId = (String) taskData.getParams().get("blobDataId");
        final String archiveName = (String) taskData.getParams().get("archiveName");
        BlobData blobData = blobDataService.get(blobDataId);
        String fileName = blobData.getName();
        String archive = archiveName == null ? "" : String.format(" (из архива \"%s\")", archiveName);
        return "Произошла ошибка при загрузке файла \"" + fileName + "\"" + archive;
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
        return AsyncTaskType.IMPORT_REF_BOOK_XML;
    }

}