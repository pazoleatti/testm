package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;


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
	public void uploadDect(@RequestParam(value = "uploader", required = true) MultipartFile file,
                           @PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws FileUploadException, IOException {
        if (declarationTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");

        TAUserInfo userInfo = securityService.currentUserInfo();
        declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
        declarationTemplateService.lock(declarationTemplateId, userInfo);

        try {
            DeclarationTemplate declarationTemplateOld = declarationTemplateService.get(declarationTemplateId);
            String jrxmBlobIdOld = declarationTemplateOld.getJrxmlBlobId();
            req.setCharacterEncoding("UTF-8");

            if (file.getSize() == 0)
                throw new ServiceException("Архив пустой.");
            DeclarationTemplate declarationTemplate = declarationTemplateImpexService.importDeclarationTemplate
                    (securityService.currentUserInfo(), declarationTemplateId, file.getInputStream());
            Date endDate = declarationTemplateService.getDTEndDate(declarationTemplateId);
            Logger customLog = new Logger();
            mainOperatingService.edit(declarationTemplate, endDate, customLog, securityService.currentUserInfo());
            IOUtils.closeQuietly(file.getInputStream());

            deleteBlobs(customLog, jrxmBlobIdOld);
            checkErrors(customLog);
            JSONObject resultUuid = new JSONObject();
            resultUuid.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(customLog.getEntries()));
            resp.getWriter().printf(resultUuid.toString());
        } catch (JSONException e) {
            logger.error(e);
            throw new ServiceException("", e);
        } finally {
            declarationTemplateService.unlock(declarationTemplateId, userInfo);
        }
	}

    @RequestMapping(value = "uploadJrxml/{declarationTemplateId}",method = RequestMethod.POST)
    @Transactional
    public void processUpload(@RequestParam(value = "uploader", required = true) MultipartFile file,
                              @PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
            throws FileUploadException, IOException {
        if (declarationTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");

        TAUserInfo userInfo = securityService.currentUserInfo();
        declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
        declarationTemplateService.lock(declarationTemplateId, userInfo);

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        InputStream inputStream = null;
        JSONObject resultUuid = new JSONObject();
        try {
            if (file.getSize() == 0)
                throw new ServiceException("Файл jrxml пустой.");
            if (file.getOriginalFilename().endsWith(".jrxml"))
                throw new ServiceException("Формат файла должен быть *.jrxml");
            inputStream = file.getInputStream();
            Date endDate = declarationTemplateService.getDTEndDate(declarationTemplateId);
            Logger customLog = new Logger();
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);

            String jrxmBlobIdOld = declarationTemplate.getJrxmlBlobId();
            String jrxmBlobId = blobDataService.create(inputStream, file.getOriginalFilename());
            resultUuid.put(UuidEnum.UUID.toString(), jrxmBlobId);
            declarationTemplate.setJrxmlBlobId(jrxmBlobId);
            declarationTemplate.setCreateScript(declarationTemplateService.getDeclarationTemplateScript(declarationTemplateId));
            mainOperatingService.edit(declarationTemplate, endDate, customLog, securityService.currentUserInfo());

            deleteBlobs(customLog, jrxmBlobIdOld);
            checkErrors(customLog);
            resultUuid.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(customLog.getEntries()));
            resp.getWriter().printf(resultUuid.toString());
        } catch (JSONException e) {
            logger.error(e);
            throw new ServiceException("", e);
        } finally {
            if (inputStream!= null){
                IOUtils.closeQuietly(inputStream);
            }
            declarationTemplateService.unlock(declarationTemplateId, userInfo);
        }
    }

    @RequestMapping(value = "uploadXsd/{declarationTemplateId}",method = RequestMethod.POST)
    @Transactional
    public void processUploadXsd(@RequestParam(value = "uploader", required = true) MultipartFile file,
                                 @PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
            throws FileUploadException, IOException {
        if (declarationTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");

        TAUserInfo userInfo = securityService.currentUserInfo();
        declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
        declarationTemplateService.lock(declarationTemplateId, userInfo);

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        JSONObject resultUuid = new JSONObject();

        try {
            if (file.getSize() == 0)
                throw new ServiceException("Файл xsd пустой.");

            Logger customLog = new Logger();
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);
            String xsdBlobIdOld = declarationTemplate.getXsdId();
            String xsdBlobId = blobDataService.create(file.getInputStream(), file.getOriginalFilename());
            declarationTemplate.setXsdId(xsdBlobId);
            resultUuid.put(UuidEnum.UUID.toString(), xsdBlobId);
            declarationTemplateService.save(declarationTemplate);

            deleteBlobs(customLog, xsdBlobIdOld);
            checkErrors(customLog);
            resultUuid.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(customLog.getEntries()));
            resp.getWriter().printf(resultUuid.toString());
        } catch (JSONException e) {
            logger.error(e);
            throw new ServiceException("", e);
        } finally {
            declarationTemplateService.unlock(declarationTemplateId, userInfo);
        }
    }

	@RequestMapping(value = "/downloadByUuid/{uuid}",method = RequestMethod.GET)
	public void processDownload(@PathVariable String uuid, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
        BlobData blobData = blobDataService.get(uuid);

        OutputStream respOut = new BufferedOutputStream(resp.getOutputStream());
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(blobData.getName(), "UTF-8") + "\"");
        resp.setCharacterEncoding("UTF-8");
        int size = IOUtils.copy(blobData.getInputStream(), respOut);
        resp.setBufferSize(size);
        IOUtils.closeQuietly(respOut);
    }

    @ExceptionHandler(ServiceLoggerException.class)
    public void logServiceExceptionHandler(ServiceLoggerException e, final HttpServletResponse response) throws IOException, JSONException {
        JSONObject errors = new JSONObject();
        response.setCharacterEncoding("UTF-8");
        errors.put(UuidEnum.ERROR_UUID.toString(), e.getUuid());
        response.getWriter().printf(errors.toString());
    }

	@ExceptionHandler(Exception.class)
	public void exceptionHandler(Exception e, final HttpServletResponse response) throws JSONException {
		response.setCharacterEncoding("UTF-8");
		logger.error(e.getLocalizedMessage(), e);
        JSONObject errors = new JSONObject();
		try {
            Logger log = new Logger();
            log.error(e.getMessage());
            errors.put(UuidEnum.ERROR_UUID.toString(), logEntryService.save(log.getEntries()));
			response.getWriter().printf(errors.toString());
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
                    log.warn(e.getMessage());
                }
            }
        }
    }

    private void checkErrors(Logger logger) throws IOException {
        if (!logger.getEntries().isEmpty()){
           throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
    }
}
