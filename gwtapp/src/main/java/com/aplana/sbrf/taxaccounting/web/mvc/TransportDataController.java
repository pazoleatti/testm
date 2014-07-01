package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TransportDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
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
import java.util.List;

@Controller
@RequestMapping("/transportDataController")
public class TransportDataController {
    private static final Log logger = LogFactory.getLog(TransportDataController.class);

    @Autowired
    LogEntryService logEntryService;

    @Autowired
    SecurityService securityService;

    @Autowired
    TransportDataService transportDataService;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void upload(HttpServletRequest request, HttpServletResponse response)
            throws FileUploadException, IOException {
        Logger logger = new Logger();

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setHeaderEncoding("UTF-8");
        List<FileItem> items = upload.parseRequest(request);

        String fileName = items.get(0).getName();
        if (fileName.contains("\\")) {
            // IE Выдает полный путь
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        }

        transportDataService.uploadFile(securityService.currentUserInfo(), ConfigurationParam.UPLOAD_DIRECTORY,
                fileName, items.get(0).getInputStream(), logger);

        if (!logger.getEntries().isEmpty()){
            response.getWriter().printf((logger.containsLevel(LogLevel.ERROR) ? "error " : "") + "uuid %s", logEntryService.save(logger.getEntries()));
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
