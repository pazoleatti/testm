package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


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
    BlobDataService blobDataService;

    @Autowired
    LogEntryService logEntryService;


    @RequestMapping(value = "declarationTemplate/downloadDect/{declarationTemplateId}",method = RequestMethod.GET)
	public void downloadDect(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String fileName = "declarationTemplate_" + declarationTemplateId + ".zip";
		resp.setContentType("application/zip");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		resp.setCharacterEncoding("UTF-8");

		declarationTemplateImpexService.exportDeclarationTemplate(securityService.currentUserInfo(), declarationTemplateId, resp.getOutputStream());
        IOUtils.closeQuietly(resp.getOutputStream());
	}


	@RequestMapping(value = "declarationTemplate/uploadDect/{declarationTemplateId}",method = RequestMethod.POST)
	public void uploadDect(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws FileUploadException, IOException {
        if (declarationTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");

        TAUserInfo userInfo = securityService.currentUserInfo();
        declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
        declarationTemplateService.lock(declarationTemplateId, userInfo);

        try {
            DeclarationTemplate declarationTemplateOld = declarationTemplateService.get(declarationTemplateId);
            String jrxmBlobIdOld = declarationTemplateOld.getJrxmlBlobId();
            String xsdUuidOld = declarationTemplateOld.getXsdId();
            req.setCharacterEncoding("UTF-8");
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> items = upload.parseRequest(req);

            if (items.get(0) != null && items.get(0).getSize() == 0)
                throw new ServiceException("Архив пустой.");
            DeclarationTemplate declarationTemplate = declarationTemplateImpexService.importDeclarationTemplate
                    (securityService.currentUserInfo(), declarationTemplateId, items.get(0).getInputStream());
            Date endDate = declarationTemplateService.getDTEndDate(declarationTemplateId);
            Logger customLog = new Logger();
            mainOperatingService.edit(declarationTemplate, endDate, customLog, securityService.currentUserInfo().getUser());
            IOUtils.closeQuietly(items.get(0).getInputStream());

            deleteBlobs(customLog, jrxmBlobIdOld, xsdUuidOld);
            checkErrors(customLog, resp);
        } finally {
            declarationTemplateService.unlock(declarationTemplateId, userInfo);
        }
	}

    @RequestMapping(value = "uploadJrxml/{declarationTemplateId}",method = RequestMethod.POST)
    @Transactional
    public void processUpload(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
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
        resp.setCharacterEncoding("UTF-8");
        InputStream inputStream = null;
        try {
            if (items.get(0) != null && items.get(0).getSize() == 0)
                throw new ServiceException("Файл jrxml пустой.");
            inputStream = items.get(0).getInputStream();
            Date endDate = declarationTemplateService.getDTEndDate(declarationTemplateId);
            Logger customLog = new Logger();
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);

            String jrxmBlobIdOld = declarationTemplate.getJrxmlBlobId();
            String jrxmBlobId = blobDataService.create(inputStream,
                    declarationTemplate.getType().getName() +"_jrxml");
            declarationTemplate.setJrxmlBlobId(jrxmBlobId);
            declarationTemplate.setCreateScript(declarationTemplateService.getDeclarationTemplateScript(declarationTemplateId));
            mainOperatingService.edit(declarationTemplate, endDate, customLog, securityService.currentUserInfo().getUser());

            deleteBlobs(customLog, jrxmBlobIdOld);
            checkErrors(customLog, resp);
        }  finally {
            if (inputStream!= null){
                IOUtils.closeQuietly(inputStream);
            }
            declarationTemplateService.unlock(declarationTemplateId, userInfo);
        }
    }

    @RequestMapping(value = "uploadXsd/{declarationTemplateId}",method = RequestMethod.POST)
    @Transactional
    public void processUploadXsd(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
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
        resp.setCharacterEncoding("UTF-8");
        InputStream inputStream = null;
        try {
            if (items.get(0) != null && items.get(0).getSize() == 0)
                throw new ServiceException("Файл xsd пустой.");
            inputStream = items.get(0).getInputStream();
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry entry = zis.getNextEntry();
            if (entry == null){
                throw new ServiceException("Архив пустой");
            }
            Logger customLog = new Logger();
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);
            String xsdBlobIdOld = declarationTemplate.getXsdId();
            String xsdBlobId = blobDataService.create(zis, entry.getName());
            declarationTemplate.setXsdId(xsdBlobId);
            declarationTemplateService.save(declarationTemplate);

            deleteBlobs(customLog, xsdBlobIdOld);
            checkErrors(customLog, resp);
        }  finally {
            if (inputStream!= null){
                IOUtils.closeQuietly(inputStream);
            }
            declarationTemplateService.unlock(declarationTemplateId, userInfo);
        }
    }

	@RequestMapping(value = "/downloadJrxml/{declarationTemplateId}",method = RequestMethod.GET)
	public void processDownloadJrxml(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
        String jrxml = declarationTemplateService.getJrxml(declarationTemplateId);
        if (jrxml == null) {
            throw new ServiceException("Файл jrxml к макету не прикреплен.");
        }
        OutputStream respOut = resp.getOutputStream();
        String fileName = "DeclarationTemplate_" + declarationTemplateId + ".jrxml";
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setCharacterEncoding("UTF-8");
        respOut.write(jrxml.getBytes("UTF-8"));
        respOut.close();
    }

    @RequestMapping(value = "/downloadXsd/{declarationTemplateId}",method = RequestMethod.GET)
    public void processDownloadXsd(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String xsd = declarationTemplateService.getXsd(declarationTemplateId);
        OutputStream respOut = resp.getOutputStream();
        String fileName = "DeclarationTemplate_" + declarationTemplateId + ".xsd";
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setCharacterEncoding("UTF-8");
        respOut.write(xsd.getBytes("UTF-8"));
        respOut.close();
    }

    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.getWriter().printf("{errorUuid : \"%s\"}", e.getUuid());
    }

	@ExceptionHandler(Exception.class)
	public void exceptionHandler(Exception e, final HttpServletResponse response) {
		response.setCharacterEncoding("UTF-8");
		logger.warn(e.getLocalizedMessage(), e);
		try {
            Logger log = new Logger();
            log.error(e.getMessage());
			response.getWriter().printf("{errorUuid : \"%s\"}", logEntryService.save(log.getEntries()));
		} catch (IOException ioException) {
			logger.error(ioException.getMessage(), ioException);
		}
	}


    private void deleteBlobs(Logger log, String... uuds){
        for (String uuid : uuds){
            if (uuid != null && !uuid.isEmpty()) {
                try {
                    blobDataService.delete(uuid);
                } catch (ServiceException e){
                    //Если вдруг не удалось удалить старую запись
                    log.warn(e.toString());
                }
            }
        }
    }

    private void checkErrors(Logger logger, HttpServletResponse response) throws IOException {
        if (!logger.getEntries().isEmpty()){
            response.getWriter().printf("{errorUuid : \"%s\"}", logEntryService.save(logger.getEntries()));
        }
    }
}
