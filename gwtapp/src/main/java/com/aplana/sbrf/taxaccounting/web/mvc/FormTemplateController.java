package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;


@Controller
public class FormTemplateController {

	private static final Log logger = LogFactory.getLog(FormTemplateController.class);

	@Autowired
	SecurityService securityService;

	@Autowired
	FormTemplateService formTemplateService;
	
	@Autowired
	FormTemplateImpexService formTemplateImpexService;

    @Autowired
    LogEntryService logEntryService;

    @Autowired
    @Qualifier("formTemplateMainOperatingService")
    private MainOperatingService mainOperatingService;
	
	
	@RequestMapping(value = "formTemplate/download/{formTemplateId}",method = RequestMethod.GET)
	public void download(@PathVariable int formTemplateId, HttpServletResponse resp)
			throws IOException {
		
		String fileName = "formTemplate_" + formTemplateId + ".zip";
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		resp.setCharacterEncoding("UTF-8");

		formTemplateImpexService.exportFormTemplate(formTemplateId, resp.getOutputStream());
		resp.getOutputStream().close();
	}
	
	
	@RequestMapping(value = "formTemplate/upload/{formTemplateId}",method = RequestMethod.POST)
	public void upload(@PathVariable int formTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws FileUploadException, IOException {
        if (formTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");
        Logger logger = new Logger();
        Date endDate = formTemplateService.getFTEndDate(formTemplateId);
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(req);
        FormTemplate formTemplate = formTemplateImpexService.importFormTemplate(formTemplateId, items.get(0).getInputStream());
        mainOperatingService.edit(formTemplate, endDate, logger, securityService.currentUserInfo().getUser());

		IOUtils.closeQuietly(items.get(0).getInputStream());
        if (!logger.getEntries().isEmpty())
            resp.getWriter().printf("uuid %s", logEntryService.save(logger.getEntries()));
	}

    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response) throws IOException {
        response.getWriter().printf("errorUuid %s", e.getUuid());
    }

	@ExceptionHandler(Exception.class)
	public void exceptionHandler(Exception e, final HttpServletResponse response) {
        /*response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);*/
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		logger.warn(e.getLocalizedMessage(), e);
		try {
            response.getWriter().printf("error %s", e.getMessage());
		} catch (IOException ioException) {
			logger.error(ioException.getMessage(), ioException);
		}
	}
}
