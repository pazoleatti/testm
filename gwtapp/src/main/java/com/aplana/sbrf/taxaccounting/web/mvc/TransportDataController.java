package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
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
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared.LoadAllResult;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Контроллер для работы с ТФ
 */
@Controller
@RequestMapping(value = "/actions/transportData")
public class TransportDataController {
    private static final Log LOG = LogFactory.getLog(TransportDataController.class);

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncManager asyncManager;

    public static final String CREATE_TASK = "Операция \"%s\" поставлена в очередь на исполнение";


    /**
     * Загрузка ТФ
     *
     * @param file ТФ
     * @return uuid группы сообщений
     * @throws IOException
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ActionResult upload(@RequestParam(value = "uploader", required = true) MultipartFile file) throws IOException {
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
                        asyncManager.executeAsync(
                                PropertyLoader.isProductionMode() ? ReportType.LOAD_ALL_TF.getAsyncTaskTypeId() : ReportType.LOAD_ALL_TF.getDevModeAsyncTaskTypeId(),
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
    @RequestMapping(value = "/loadAll", method = RequestMethod.POST)
    @ResponseBody
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
                    asyncManager.executeAsync(
                            PropertyLoader.isProductionMode() ? ReportType.LOAD_ALL_TF.getAsyncTaskTypeId() : ReportType.LOAD_ALL_TF.getDevModeAsyncTaskTypeId(),
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


    /**
     * Обработка исключений, связанных с тем, что скрипт выполнился с ошибками
     *
     * @param e        исключение
     * @param response ответ
     * @throws IOException
     */
    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response)
            throws IOException {
        response.getWriter().printf("error uuid %s", e.getUuid());
    }

    /**
     * Обработка стандартных исключений
     *
     * @param e        исключение
     * @param response ответ
     */
    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception e, final HttpServletResponse response) {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        LOG.warn(e.getLocalizedMessage(), e);
        try {
            Logger logger = new Logger();
            logger.error("Ошибка: " + e.getMessage());
            response.getWriter().printf("error uuid %s", logEntryService.save(logger.getEntries()));
        } catch (IOException ioException) {
            LOG.error(ioException.getMessage(), ioException);
        }
    }
}
