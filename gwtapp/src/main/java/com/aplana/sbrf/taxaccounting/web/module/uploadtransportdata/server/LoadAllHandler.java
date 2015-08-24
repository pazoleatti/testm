package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.server;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LoadFormDataService;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllAction;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER','ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class LoadAllHandler extends AbstractActionHandler<LoadAllAction, LoadAllResult> {

    @Autowired
    LoadRefBookDataService loadRefBookDataService;

    @Autowired
    LoadFormDataService loadFormDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;

    static final public String TASK_NAME = "Импорт транспортного файла из каталога загрузки";

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
        BalancingVariants balancingVariant = BalancingVariants.SHORT;
        if (!action.isForce()) {
            Logger localLogger = new Logger();
            List<TransportFileInfo> files = loadRefBookDataService.getRefBookTransportDataFiles(userInfo, localLogger);
            files.addAll(loadFormDataService.getFormDataFiles(userInfo, loadFormDataService.getTB(userInfo, localLogger), null, localLogger));
            if (files.isEmpty()) {
                logger.error("Нет файлов для обработки");
                throw new ServiceLoggerException("Выполнение операции \"%s\" невозможно, т.к. нет файлов для обработки!", logEntryService.save(logger.getEntries()), TASK_NAME);
            }

            AsyncTaskTypeData taskTypeData = asyncTaskTypeDao.get(ReportType.LOAD_ALL_TF.getAsyncTaskTypeId(true));
            if (taskTypeData.getTaskLimit() > 0) {
                long maxFileSize = taskTypeData.getTaskLimit();
                int skip = 0;

                for (TransportFileInfo file : files) {
                    if (!action.isForce() && file.getLength() > maxFileSize) {
                        logger.error("ТФ \"%s\", путь к каталогу загрузки \"%s\", размер ТФ %s Кбайт", file.getName(), file.getPath(), file.getLength());
                        skip++;
                    } else if (taskTypeData.getShortQueueLimit() != 0 && file.getLength() > taskTypeData.getShortQueueLimit()) {
                        balancingVariant = BalancingVariants.LONG;
                    }

                }
                if (skip == files.size()) {
                    logger.error("Критерии возможности выполнения задач задаются в конфигурационных параметрах (параметры асинхронных заданий). За разъяснениями обратитесь к Администратору");
                    result.setDialogMsg(String.format("Выполнение операции \"%s\" невозможно, т.к. размер всех ТФ превышает максимально допустимый (%s Кбайт)!", TASK_NAME, taskTypeData.getTaskLimit()));
                    result.setUuid(logEntryService.save(logger.getEntries()));
                    return result;
                }
                if (logger.containsLevel(LogLevel.ERROR)) {
                    logger.error("Критерии возможности выполнения задач задаются в конфигурационных параметрах (параметры асинхронных заданий). За разъяснениями обратитесь к Администратору");
                    result.setDialogMsg(String.format("Выполнение операции \"%s\" для части ТФ невозможно, т.к. их размер превышает максимально допустимый (%s Кбайт). Продолжить?", TASK_NAME, taskTypeData.getTaskLimit()));
                    result.setFileSizeLimit(true);
                    result.setUuid(logEntryService.save(logger.getEntries()));
                    return result;
                }
            }
        }
        LockData lockData = lockDataService.lock(key, userId,
                LockData.DescriptionTemplate.LOAD_TRANSPORT_DATA.getText(),
                LockData.State.IN_QUEUE.getText(),
                lockDataService.getLockTimeout(LockData.LockObjects.LOAD_TRANSPORT_DATA));
        if (lockData == null) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(AsyncTask.RequiredParams.USER_ID.name(), userId);
                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                lockData = lockDataService.getLock(key);
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                try {
                    lockDataService.addUserWaitingForLock(key, userId);
                    asyncManager.executeAsync(ReportType.LOAD_ALL_TF.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params, balancingVariant);
                    lockDataService.updateQueue(key, lockData.getDateLock(), balancingVariant);
                    logger.info("Задача загрузки ТФ запущена");
                } catch (AsyncTaskException e) {
                    lockDataService.unlock(key, userId);
                    logger.error("Ошибка при постановке в очередь задачи загрузки ТФ.");
                }
            } catch(Exception e) {
                try {
                    lockDataService.unlock(key, userId);
                } catch (ServiceException e2) {
                    if (PropertyLoader.isProductionMode() || !(e instanceof RuntimeException)) { // в debug-режиме не выводим сообщение об отсутсвии блокировки, если оня снята при выбрасывании исключения
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
