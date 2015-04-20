package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
public class TransportDataController {
    private static final Log logger = LogFactory.getLog(TransportDataController.class);

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private AsyncManager asyncManager;

    @RequestMapping(value = "transportData/upload", method = RequestMethod.POST)
    public void upload(HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
        Logger logger = new Logger();

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setHeaderEncoding("UTF-8");

        List<FileItem> items = upload.parseRequest(request);

        if (items == null || items.isEmpty()) {
            throw new ServiceException("Не указан файл для загрузки!");
        }

        String fileName = items.get(0).getName();
        if (fileName.contains("\\")) {
            // IE Выдает полный путь
            fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        }

        TAUserInfo userInfo = securityService.currentUserInfo();
        int userId = userInfo.getUser().getId();

        String key = LockData.LockObjects.LOAD_TRANSPORT_DATA.name() + "_" + UUID.randomUUID().toString().toLowerCase();
        LockData lockData = lockDataService.lock(key, userId,
                lockDataService.getLockTimeout(LockData.LockObjects.LOAD_TRANSPORT_DATA));
        if (lockData == null) {
            String uuidFile = blobDataService.create(items.get(0).getInputStream(), fileName);
            try {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("uuidFile", uuidFile);
                params.put(AsyncTask.RequiredParams.USER_ID.name(), userId);
                params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockDataService.getLock(key).getDateLock());
                try {
                    lockDataService.addUserWaitingForLock(key, userId);
                    asyncManager.executeAsync(ReportType.UPLOAD_TF.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params, BalancingVariants.LONG);
                    logger.info("Задача загрузки ТФ запущена");
                } catch (AsyncTaskException e) {
                    lockDataService.unlock(key, userId);
                    logger.error("Ошибка при постановке в очередь задачи формирования декларации.");
                }
            } catch(Exception e) {
                try {
                    blobDataService.delete(uuidFile);
                } catch(Exception e1) {}
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

        if (!logger.getEntries().isEmpty()) {
            response.getWriter().printf("uuid %s", logEntryService.save(logger.getEntries()));
        }
    }

    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response)
            throws IOException {
        response.getWriter().printf("error uuid %s", e.getUuid());
    }

    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception e, final HttpServletResponse response) {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        logger.warn(e.getLocalizedMessage(), e);
        try {
            Logger logger = new Logger();
            logger.error("Ошибка: " + e.getMessage());
            response.getWriter().printf("error uuid %s", logEntryService.save(logger.getEntries()));
        } catch (IOException ioException) {
            logger.error(ioException.getMessage(), ioException);
        }
    }
}
