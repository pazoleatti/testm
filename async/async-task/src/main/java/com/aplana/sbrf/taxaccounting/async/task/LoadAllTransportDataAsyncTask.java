package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCK_DATE;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

/**
 * Реализация таска "Загрузка файлов"
 * @author Lhaziev
 */
public abstract class LoadAllTransportDataAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    BlobDataService blobDataService;

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private LoadDeclarationDataService loadDeclarationDataService;

    @Override
    protected ReportType getReportType() {
        return ReportType.LOAD_ALL_TF;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) {
        return BalancingVariants.LONG;
    }

    private String msg;

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        if (params.containsKey("blobDataId")) {
            final String blobDataId = (String) params.get("blobDataId");
            BlobData blobData = blobDataService.get(blobDataId);
            lockDataService.updateState(lock, lockDate, "Загрузка файлов");
            msg = loadDeclarationDataService.uploadFile(logger, userInfo, blobData.getName(), blobData.getInputStream(), lock);
        } else {
            String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
            lockDataService.lock(key, userInfo.getUser().getId(),
                    LockData.DescriptionTemplate.CONFIGURATION_PARAMS.getText());
            try {
                logger.info("Номер загрузки: %s", lock);
                // Справочники
                loadRefBookDataService.checkImportRefBookTransportData(userInfo, logger, lock, lockDate, true);
            } finally {
                lockDataService.unlock(key, userInfo.getUser().getId());
            }
        }
        return new TaskStatus(true, null);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Загрузка файлов";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        if (params.containsKey("blobDataId")) {
            final String blobDataId = (String) params.get("blobDataId");
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
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
        if (params.containsKey("blobDataId")) {
            final String blobDataId = (String) params.get("blobDataId");
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
}
