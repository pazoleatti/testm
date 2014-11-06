package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;


@Controller
public class DeclarationTemplateController {

	private static final Log logger = LogFactory.getLog(DeclarationTemplateController.class);

	@Autowired
	SecurityService securityService;

	@Autowired
	DeclarationTemplateService declarationTemplateService;
	
	@Autowired
	DeclarationTemplateImpexService declarationTemplateImpexService;

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    private MainOperatingService mainOperatingService;

    @Autowired
    LogEntryService logEntryService;

    private static final String RESP_CONTENT_TYPE_PLAIN = "text/plain";


    @RequestMapping(value = "declarationTemplate/downloadDect/{declarationTemplateId}",method = RequestMethod.GET)
	public void downloadDect(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String fileName = "declarationTemplate_" + declarationTemplateId + ".zip";
		resp.setContentType("application/dect");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		resp.setCharacterEncoding("UTF-8");
		
		declarationTemplateImpexService.exportDeclarationTemplate(securityService.currentUserInfo(), declarationTemplateId, resp.getOutputStream());
		resp.getOutputStream().close();	
	}
	
	
	@RequestMapping(value = "declarationTemplate/uploadDect/{declarationTemplateId}",method = RequestMethod.POST)
	public void uploadDect(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws FileUploadException, IOException {
        if (declarationTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");

        TAUserInfo userInfo = securityService.currentUserInfo();
        declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
        declarationTemplateService.lock(declarationTemplateId, userInfo);

        req.setCharacterEncoding("UTF-8");
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = upload.parseRequest(req);

        if (items.get(0) != null && items.get(0).getSize() == 0)
            throw new ServiceException("Файл jrxml пустой.");
		DeclarationTemplate declarationTemplate = declarationTemplateImpexService.importDeclarationTemplate
                (securityService.currentUserInfo(), declarationTemplateId, items.get(0).getInputStream());
        Date endDate = declarationTemplateService.getDTEndDate(declarationTemplateId);
        Logger customLog = new Logger();
        mainOperatingService.edit(declarationTemplate, endDate, customLog, securityService.currentUserInfo().getUser());
		IOUtils.closeQuietly(items.get(0).getInputStream());
        if (!customLog.getEntries().isEmpty()){
            resp.setContentType(RESP_CONTENT_TYPE_PLAIN);
            resp.getWriter().printf("uuid %s", logEntryService.save(customLog.getEntries()));
        }
	}

	@RequestMapping(value = "/downloadJrxml/{declarationTemplateId}",method = RequestMethod.GET)
	public void processDownload(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
        String jrxml = declarationTemplateService.getJrxml(declarationTemplateId);
        if (jrxml == null) {
            jrxml = "";
        }
        OutputStream respOut = resp.getOutputStream();
        String fileName = "DeclarationTemplate_" + declarationTemplateId + ".jrxml";
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setCharacterEncoding("UTF-8");
        respOut.write(jrxml.getBytes("UTF-8"));
        respOut.close();
    }

	@RequestMapping(value = "uploadJrxml/{declarationTemplateId}",method = RequestMethod.POST)
	public void processUpload(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws FileUploadException, UnsupportedEncodingException {
        if (declarationTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");
        req.setCharacterEncoding("UTF-8");
        FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<FileItem> items = upload.parseRequest(req);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        try {
            if (items.get(0) != null && items.get(0).getSize() == 0)
                throw new ServiceException("Файл jrxml пустой.");
            InputStream inputStream = items.get(0).getInputStream();
            declarationTemplateService.setJrxml(declarationTemplateId, inputStream);
            inputStream.close();
        } catch (ServiceException e){
            exceptionHandler(e, resp);
        } catch (IOException e) {
            exceptionHandler(e, resp);
        }
    }

    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response) throws IOException {
        response.setContentType(RESP_CONTENT_TYPE_PLAIN);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf("errorUuid %s", e.getUuid());
    }

	@ExceptionHandler(Exception.class)
	public void exceptionHandler(Exception e, final HttpServletResponse response) {
		response.setContentType(RESP_CONTENT_TYPE_PLAIN);
		response.setCharacterEncoding("UTF-8");
		logger.warn(e.getLocalizedMessage(), e);
		try {
			response.getWriter().append("error ").append(e.getMessage()).close();
		} catch (IOException ioException) {
			logger.error(ioException.getMessage(), ioException);
		}
	}
}
