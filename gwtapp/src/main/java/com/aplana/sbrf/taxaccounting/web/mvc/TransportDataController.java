package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.UploadTransportDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.fileupload.FileUploadException;
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

@Controller
public class TransportDataController {
    private static final Log LOG = LogFactory.getLog(TransportDataController.class);

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UploadTransportDataService uploadTransportDataService;

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

        // Загрузка в каталог
        //UploadResult uploadResult =
		uploadTransportDataService.uploadFile(userInfo, fileName, file.getInputStream(), logger);

        // Загрузка из каталога
        /*if (!uploadResult.getDiasoftFileNameList().isEmpty()) {
            // Diasoft
            loadRefBookDataService.importRefBookDiasoft(userInfo, uploadResult.getDiasoftFileNameList(), logger);
        }
        if (!uploadResult.getAvgCostFileNameList().isEmpty()) {
            loadRefBookDataService.importRefBookAvgCost(userInfo, uploadResult.getAvgCostFileNameList(), logger);
        }

        if (!uploadResult.getFormDataFileNameList().isEmpty()) {
            // НФ
            // Пересечение списка доступных приложений и списка загруженных приложений
            List<Integer> departmentList = new ArrayList(CollectionUtils.intersection(
                    loadFormDataService.getTB(userInfo, logger), uploadResult.getFormDataDepartmentList()));

            loadFormDataService.importFormData(userInfo, departmentList, uploadResult.getFormDataFileNameList(), logger);
        }*/

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
