package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Stanislav Yasinskiy
 */

@Controller
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class BookerStatementsController {

    @Autowired
    BookerStatementsService service;

    private static final Log logger = LogFactory.getLog(BookerStatementsController.class);

    @RequestMapping(value = "/bookerstatements/{departmentId}/{periodId}/{typeId}", method = RequestMethod.POST)
    public void processUpload(@PathVariable Integer departmentId, @PathVariable Integer periodId,  @PathVariable int typeId,
                              HttpServletRequest request, HttpServletResponse response) throws FileUploadException, IOException, ServiceException {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);
        service.importXML(items.get(0).getInputStream(), periodId, typeId, departmentId);
    }

    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception e, final HttpServletResponse response) {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        logger.warn(e.getLocalizedMessage(), e);
        try {
            response.getWriter().append("error ").append(e.getMessage()).close();
        } catch (IOException ioException) {
            logger.error(ioException.getMessage(), ioException);
        }
    }
}
