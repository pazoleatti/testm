package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.service.AsyncTaskInterceptor;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LoadFormDataService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCKED_OBJECT;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.LOCK_DATE;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

/**
 * Реализация таска "Загрузка ТФ из каталога загрузки"
 * @author Lhaziev
 */
public abstract class LoadAllTransportDataAsyncTask extends AbstractAsyncTask {

    // private final Log log = LogFactory.getLog(getClass());

    @Autowired
    TAUserService userService;

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LoadFormDataService loadFormDataService;

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private LockDataService lockService;

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) {
        return BalancingVariants.LONG;
    }

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        lockDataService.lock(key, userInfo.getUser().getId(),
                LockData.DescriptionTemplate.CONFIGURATION_PARAMS.getText(),
                lockDataService.getLockTimeout(LockData.LockObjects.CONFIGURATION_PARAMS));
        try {
            // Diasoft
            lockService.updateState(lock, lockDate, "Импорт справочников \"Diasoft\"");
            loadRefBookDataService.importRefBookDiasoft(userInfo, logger);

            lockService.updateState(lock, lockDate, "Импорт справочника \"Средняя стоимость транспортных средств\"");
            loadRefBookDataService.importRefBookAvgCost(userInfo, logger);

            // НФ
            lockService.updateState(lock, lockDate, "Импорт налоговых форм");
            loadFormDataService.importFormData(userInfo, loadFormDataService.getTB(userInfo, logger), null, logger);
        } finally {
            lockDataService.unlock(key, userInfo.getUser().getId());
        }
    }

    @Override
    protected String getAsyncTaskName() {
        return "Загрузка ТФ из каталога загрузки";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return "Загрузка ТФ из каталога загрузки завершена";
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params) {
        return "Произошла непредвиденная ошибка при загрузке ТФ из каталога загрузкиа";
    }
}
