package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.server;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllAction;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP', 'ROLE_ADMIN')")
public class LoadAllHandler extends AbstractActionHandler<LoadAllAction, LoadAllResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private ApplicationInfo applicationInfo;

    public LoadAllHandler() {
        super(LoadAllAction.class);
    }

    @Override
    public LoadAllResult execute(LoadAllAction action, ExecutionContext context) throws ActionException {
        Logger logger = new Logger();
        LoadAllResult result = new LoadAllResult();

        TAUserInfo userInfo = securityService.currentUserInfo();
        int userId = userInfo.getUser().getId();
        String key = LockData.LockObjects.LOAD_TRANSPORT_DATA.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        LockData lockData = lockDataService.lock(key, userId, DescriptionTemplate.LOAD_TRANSPORT_DATA.getText());
        if (lockData == null) {
            try {
                try {
                    AsyncTaskData taskData = asyncManager.executeTask(key, AsyncTaskType.LOAD_ALL_TF, userInfo, AsyncQueue.LONG);
                    asyncManager.addUserWaitingForTask(taskData.getId(), userId);
                    logger.info("Задача загрузки ТФ запущена");
                } catch (AsyncTaskException e) {
                    lockDataService.unlock(key, userId);
                    logger.error("Ошибка при постановке в очередь задачи загрузки ТФ.");
                }
            } catch(Exception e) {
                try {
                    lockDataService.unlock(key, userId);
                } catch (ServiceException e2) {
                    if (applicationInfo.isProductionMode() || !(e instanceof RuntimeException)) { // в debug-режиме не выводим сообщение об отсутсвии блокировки, если оня снята при выбрасывании исключения
                        throw e2;
                    }
                }
                if (e instanceof ServiceLoggerException) {
                    throw new ServiceLoggerException(e.getMessage(), ((ServiceLoggerException) e).getUuid());
                } else {
                    throw new ServiceException(e.getMessage(), e);
                }
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(LoadAllAction action, LoadAllResult result, ExecutionContext context) throws ActionException {
    }
}
