package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

public abstract class UploadRefBookAsyncTask extends AbstractAsyncTask {

    @Autowired
    private TAUserService userService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private LockDataService lockService;

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;

    @Override
    protected ReportType getReportType() {
        return ReportType.IMPORT_REF_BOOK;
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        int userId = (Integer)params.get(USER_ID.name());
        long refBookId = (Long)params.get("refBookId");
        String uuid = (String)params.get("uuid");

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        Long value = (long)Math.ceil(blobDataService.getLength(uuid) / 1024.);
        String msg = String.format("выбранный файл имеет слишком большой размер (%s Кбайт)!",  value);
        return checkTask(getReportType(), value, refBookFactory.getTaskName(getReportType(), refBookId, null), msg);
    }

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        long refBookId = (Long)params.get("refBookId");
        String uuid = (String)params.get("uuid");
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());
        Date dateFrom = null;
        if (params.containsKey("dateFrom")) {
            dateFrom = (Date)params.get("dateFrom");
        }
        Date dateTo = null;
        if (params.containsKey("dateTo")) {
            dateTo = (Date)params.get("dateTo");
        }

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        BlobData blobData = blobDataService.get(uuid);
        logger.info("Загрузка данных из файла: \"%s\"", blobData.getName());
        //Парсит загруженный в файловое хранилище файл
        loadRefBookDataService.importRefBook(logger, userInfo,
                refBookId, blobData.getInputStream(), blobData.getName(), dateFrom, dateTo, new LockStateLogger() {
                    @Override
                    public void updateState(String state) {
                        lockService.updateState(lock, lockDate, state);
                    }
                });
        if (logger.containsLevel(LogLevel.ERROR)) {
            return new TaskStatus(false, null);
        }
        return new TaskStatus(true, null);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Загрузка данных из файла в справочник";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        long refBookId = (Long)params.get("refBookId");
        return String.format("Успешно выполнена загрузка данных из файла в справочник \"%s\"", refBookFactory.get(refBookId).getName());
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
        long refBookId = (Long)params.get("refBookId");
        return String.format("Не выполнена загрузка данных из файла в справочник \"%s\"", refBookFactory.get(refBookId).getName());
    }
}
