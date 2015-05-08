package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.UploadResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.USER_ID;

/**
 * Спринговая реализация таска "Загрузка ТФ из каталога загрузки" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("LoadAllTransportDataAsyncTaskSpring")
public class LoadAllTransportDataAsyncTaskSpring extends AbstractAsyncTask {

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

    @Override
    protected void executeBusinessLogic(Map<String, Object> params, Logger logger) {
        log.debug("LoadAllTransportDataAsyncTaskSpring has been started!");

        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));

        String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        lockDataService.lock(key, userInfo.getUser().getId(),
                LockData.DescriptionTemplate.CONFIGURATION_PARAMS.getText(),
                lockDataService.getLockTimeout(LockData.LockObjects.CONFIGURATION_PARAMS));
        try {
            // Diasoft
            loadRefBookDataService.importRefBookDiasoft(userInfo, logger);

            loadRefBookDataService.importRefBookAvgCost(userInfo, logger);

            // НФ
            loadFormDataService.importFormData(userInfo, loadFormDataService.getTB(userInfo, logger), null, logger);
        } finally {
            lockDataService.unlock(key, userInfo.getUser().getId());
        }
        log.debug("LoadAllTransportDataAsyncTaskSpring has been finished");

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
