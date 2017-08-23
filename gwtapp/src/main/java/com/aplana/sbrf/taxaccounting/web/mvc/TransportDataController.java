package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.model.ActionResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для работы с ТФ
 */
@RestController
public class TransportDataController {
    private static final String CREATE_TASK = "Операция \"%s\" поставлена в очередь на исполнение";

    private final LogEntryService logEntryService;
    private final SecurityService securityService;
    private final BlobDataService blobDataService;
    private final LockDataService lockDataService;
    private final AsyncManager asyncManager;

    public TransportDataController(LogEntryService logEntryService, SecurityService securityService, BlobDataService blobDataService, LockDataService lockDataService, AsyncManager asyncManager) {
        this.logEntryService = logEntryService;
        this.securityService = securityService;
        this.blobDataService = blobDataService;
        this.lockDataService = lockDataService;
        this.asyncManager = asyncManager;
    }


    /**
     * Загрузка ТФ
     *
     * @param file ТФ
     * @return uuid группы сообщений
     * @throws IOException IOException
     */
    @PostMapping(value = "/actions/transportData/upload")
    public ActionResult upload(@RequestParam(value = "uploader") MultipartFile file) throws IOException {
        Logger logger = new Logger();

        if (file == null || file.isEmpty()) {
            throw new ServiceException("Не указан файл для загрузки!");
        }

        String fileName = file.getOriginalFilename();
        if (fileName.contains("\\")) {
            // IE Выдает полный путь
            fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        }

        TAUserInfo userInfo = securityService.currentUserInfo();
        InputStream inputStream = file.getInputStream();
        try {
            String uuid = blobDataService.create(inputStream, fileName);

            int userId = userInfo.getUser().getId();
            String key = LockData.LockObjects.LOAD_TRANSPORT_DATA.name() + "_" + UUID.randomUUID().toString().toLowerCase();
            BalancingVariants balancingVariant = BalancingVariants.SHORT;
            LockData lockData = lockDataService.lock(key, userId,
                    String.format(LockData.DescriptionTemplate.IMPORT_TRANSPORT_DATA.getText(), fileName),
                    LockData.State.IN_QUEUE.getText());
            if (lockData == null) {
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put(AsyncTask.RequiredParams.USER_ID.name(), userId);
                    params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                    params.put("blobDataId", uuid);

                    lockData = lockDataService.getLock(key);
                    params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                    try {
                        lockDataService.addUserWaitingForLock(key, userId);
                        asyncManager.executeAsync(ReportType.LOAD_ALL_TF.getAsyncTaskTypeId(),
                                params, balancingVariant);
                        LockData.LockQueues queue = LockData.LockQueues.getById(balancingVariant.getId());
                        lockDataService.updateQueue(key, lockData.getDateLock(), queue);
                        logger.info(String.format(CREATE_TASK, "Загрузка файла"));
                    } catch (AsyncTaskException e) {
                        lockDataService.unlock(key, userId);
                        logger.error("Ошибка при постановке в очередь задачи загрузки ТФ.");
                    }
                } catch (Exception e) {
                    logger.error(e);
                    try {
                        lockDataService.unlock(key, userId);
                    } catch (ServiceException e2) {
                        logger.error(e2);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        ActionResult result = new ActionResult();
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    /**
     * Загрузка всех файлов из каталога загрузки
     *
     * @return uuid группы сообщений
     */
    @PostMapping(value = "/actions/transportData/loadAll")
    public ActionResult loadAll() {
        Logger logger = new Logger();

        TAUserInfo userInfo = securityService.currentUserInfo();
        int userId = userInfo.getUser().getId();
        String key = LockData.LockObjects.LOAD_TRANSPORT_DATA.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        BalancingVariants balancingVariant = BalancingVariants.SHORT;
        LockData lockData = lockDataService.lock(key, userId, LockData.DescriptionTemplate.LOAD_TRANSPORT_DATA.getText(),
                LockData.State.IN_QUEUE.getText());
        if (lockData == null) {
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(AsyncTask.RequiredParams.USER_ID.name(), userId);
                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                lockData = lockDataService.getLock(key);
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                try {
                    lockDataService.addUserWaitingForLock(key, userId);
                    asyncManager.executeAsync(ReportType.LOAD_ALL_TF.getAsyncTaskTypeId(),
                            params, balancingVariant);
                    LockData.LockQueues queue = LockData.LockQueues.getById(balancingVariant.getId());
                    lockDataService.updateQueue(key, lockData.getDateLock(), queue);
                    logger.info("Задача загрузки ТФ запущена");
                } catch (AsyncTaskException e) {
                    lockDataService.unlock(key, userId);
                    logger.error("Ошибка при постановке в очередь задачи загрузки ТФ.");
                }
            } catch (Exception e) {
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

        ActionResult result = new ActionResult();
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }
}
