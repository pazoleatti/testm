package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
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
    private RefBookFactory refBookFactory;

    @Override
    public String getDescription(TAUserInfo userInfo, Map<String, Object> params) {
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
        BlobData blobData = blobDataService.get(blobDataId);
        String fileName = blobData.getName();
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        return String.format("Загрузка файла \"%s\" в справочник \"%s\" завершена", fileName, refBookFactory.get(refBookId).getName());
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        final String blobDataId = (String) taskData.getParams().get("blobDataId");
        BlobData blobData = blobDataService.get(blobDataId);
        String fileName = blobData.getName();
        return "Произошла ошибка при загрузке файла \"" + fileName + "\"";
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
        return AsyncTaskType.IMPORT_REF_BOOK_XML;
    }
}