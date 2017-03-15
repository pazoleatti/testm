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
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
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


    @RequestMapping(value = "transportData/upload", method = RequestMethod.POST)
    public void upload(@RequestParam(value = "uploader", required = true) MultipartFile file,
                       HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
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
                        logger.info(String.format(CREATE_TASK, "Загрузки файла"));
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
