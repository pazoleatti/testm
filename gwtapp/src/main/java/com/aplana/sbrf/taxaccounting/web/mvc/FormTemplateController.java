package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class FormTemplateController {

	private static final Log LOG = LogFactory.getLog(FormTemplateController.class);

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
		try {
			formTemplateImpexService.exportFormTemplate(formTemplateId, resp.getOutputStream());
		} finally {
			IOUtils.closeQuietly(resp.getOutputStream());
		}
	}

    @RequestMapping(value = "formTemplate/downloadAll")
    public void downloadAll(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition",
                String.format("attachment; filename=\"Templates(%s).zip\"",
                        new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        );
        response.setCharacterEncoding("UTF-8");
		try {
        	formTemplateImpexService.exportAllTemplates(response.getOutputStream());
		} finally {
        	IOUtils.closeQuietly(response.getOutputStream());
		}
    }
	
	
	@RequestMapping(value = "formTemplate/upload/{formTemplateId}",method = RequestMethod.POST)
	public void upload(@RequestParam("uploader") MultipartFile file,
                       @PathVariable int formTemplateId, HttpServletResponse resp)
			throws FileUploadException, IOException {
        if (formTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");

        TAUserInfo userInfo = securityService.currentUserInfo();
        formTemplateService.checkLockedByAnotherUser(formTemplateId, userInfo);
        formTemplateService.lock(formTemplateId, userInfo);

        try {
            Logger logger = new Logger();
            Date endDate = formTemplateService.getFTEndDate(formTemplateId);
			try {
				FormTemplate formTemplate = formTemplateImpexService.importFormTemplate(formTemplateId, file.getInputStream());
				mainOperatingService.edit(formTemplate, endDate, logger, securityService.currentUserInfo());
			} finally {
            	IOUtils.closeQuietly(file.getInputStream());
			}
            resp.setContentType("text/plain");
            resp.setCharacterEncoding("UTF-8");
            /*JSONObject result = new JSONObject();
            result.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(logger.getEntries()));
            resp.getWriter().printf(result.toString());*/

            resp.getWriter().printf("uuid %s",  logEntryService.save(logger.getEntries()));
        } finally {
            if (formTemplateService.unlock(formTemplateId, userInfo)){
                LOG.warn(String.format("Не разблокировалась запись %d в макетах НФ", formTemplateId));
            }
        }
    }

    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf("errorUuid %s", e.getUuid());
    }

	@ExceptionHandler(Exception.class)
	public void exceptionHandler(Exception e, final HttpServletResponse response) {
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		LOG.warn(e.getLocalizedMessage(), e);
		try {
            response.getWriter().printf("error %s", e.getMessage());
		} catch (IOException ioException) {
			LOG.error(ioException.getMessage(), ioException);
		}
	}
}
