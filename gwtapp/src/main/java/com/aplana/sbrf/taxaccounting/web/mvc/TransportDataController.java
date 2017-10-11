package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.model.ActionResult;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ApplicationInfo applicationInfo;

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
            AsyncQueue queue = AsyncQueue.SHORT;
            LockData lockData = lockDataService.lock(key, userId,
                    String.format(DescriptionTemplate.IMPORT_TRANSPORT_DATA.getText(), fileName));
            if (lockData == null) {
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("blobDataId", uuid);

                    try {
                        asyncManager.executeTask(key, AsyncTaskType.LOAD_ALL_TF, userInfo, queue, params);
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
            } catch (Exception e) {
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

        ActionResult result = new ActionResult();
        if (!logger.getEntries().isEmpty()) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }
}
