package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

public abstract class CheckDeclarationAsyncTask extends AbstractDeclarationAsyncTask {

    private static final String SUCCESS = "Выполнена проверка налоговой формы: %s";
    private static final String FAIL = "Выполнена проверка налоговой формы: %s. Найдены фатальные ошибки.";

    @Autowired
    private TAUserService userService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private LockDataService lockService;

    @Override
    protected ReportType getReportType() {
        return ReportType.CHECK_DEC;
    }

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) {
        long declarationDataId = (Long)params.get("declarationDataId");
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());

        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        if (declarationData != null) {
            declarationDataService.check(logger, declarationDataId, userInfo, new LockStateLogger() {
                @Override
                public void updateState(String state) {
                    lockService.updateState(lock, lockDate, state);
                }
            });
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            return new TaskStatus(false, null);
        }
        return new TaskStatus(true, null);
    }

    @Override
    protected String getAsyncTaskName() {
        return "Проверикак налоговой формы";
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
        return getMessage(params, false, unexpected);
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return getMessage(params, true, false);
    }

    private String getMessage(Map<String, Object> params, boolean isSuccess, boolean unexpected) {
        String template = isSuccess ? SUCCESS : FAIL;
        return String.format(template,
                getDeclarationDescription(params));
    }
}
