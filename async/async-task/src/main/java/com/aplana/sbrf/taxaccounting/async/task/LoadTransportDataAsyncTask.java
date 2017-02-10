package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

/**
 * Реализация таска "Загрузка ТФ из каталога загрузки"
 * @author Lhaziev
 */
public abstract class LoadTransportDataAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    BlobDataService blobDataService;

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

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());
        final String blobDataId = (String) params.get("blobDataId");

        BlobData blobData = blobDataService.get(blobDataId);
        lockDataService.updateState(lock, lockDate, "Загрузка файлов");
        loadDeclarationDataService.uploadFile(logger, userInfo, blobData.getName(), blobData.getInputStream(), lock);
        return new TaskStatus(true, null);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Загрузка файлов";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return "Загрузка файлов";
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
        return "Загрузка файлов";
    }
}
