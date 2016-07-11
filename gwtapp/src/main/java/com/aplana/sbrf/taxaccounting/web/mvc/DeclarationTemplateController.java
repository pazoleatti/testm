package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
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
import java.io.*;
import java.net.URLEncoder;
import java.util.Date;

@Controller
public class DeclarationTemplateController {

	private static final Log LOG = LogFactory.getLog(DeclarationTemplateController.class);
    private static final String ENCODING = "UTF-8";

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
        if (checkRole(resp, securityService.currentUserInfo())) {
            String fileName = "declarationTemplate_" + declarationTemplateId + ".zip";
            resp.setContentType("application/zip");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            resp.setCharacterEncoding("UTF-8");
            try {
                declarationTemplateImpexService.exportDeclarationTemplate(securityService.currentUserInfo(), declarationTemplateId, resp.getOutputStream());
            } finally {
                IOUtils.closeQuietly(resp.getOutputStream());
            }
        }
	}

	@RequestMapping(value = "declarationTemplate/uploadDect/{declarationTemplateId}",method = RequestMethod.POST)
	public void uploadDect(@RequestParam(value = "uploader", required = true) MultipartFile file,
                           @PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws FileUploadException, IOException {
        if (checkRole(resp, securityService.currentUserInfo())) {
            if (declarationTemplateId == 0)
                throw new ServiceException("Сначала сохраните шаблон.");

            TAUserInfo userInfo = securityService.currentUserInfo();
            declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
            declarationTemplateService.lock(declarationTemplateId, userInfo);

            try {
                req.setCharacterEncoding("UTF-8");

                if (file.getSize() == 0)
                    throw new ServiceException("Архив пустой.");
                DeclarationTemplate declarationTemplate = declarationTemplateImpexService.importDeclarationTemplate
                        (securityService.currentUserInfo(), declarationTemplateId, file.getInputStream());
                //http://jira.aplana.com/browse/SBRFACCTAX-12066
                Logger logger = new Logger();
                logger.setTaUserInfo(securityService.currentUserInfo());


                Date endDate = declarationTemplateService.getDTEndDate(declarationTemplateId);

                if (declarationTemplate.getStatus().equals(VersionedObjectStatus.NORMAL)) {
                    mainOperatingService.isInUsed(
                            declarationTemplateId,
                            declarationTemplate.getType().getId(),
                            declarationTemplate.getStatus(),
                            declarationTemplate.getVersion(),
                            endDate,
                            logger);
                    checkErrors(logger);
                }

                //Проверка на использоваение jrxml другими декларациями
                //http://jira.aplana.com/browse/SBRFACCTAX-12066
                if (
                        declarationTemplate.getJrxmlBlobId() != null
                                &&
                                declarationTemplateService.checkExistingDataJrxml(declarationTemplateId, logger)) {
                    JSONObject resultUuid = new JSONObject();
                    String uploadUuid = blobDataService.create(file.getInputStream(), file.getName());
                    resultUuid.put(UuidEnum.ERROR_UUID.toString(), logEntryService.save(logger.getEntries()));
                    resultUuid.put(UuidEnum.UPLOADED_FILE.toString(), uploadUuid);

                    resp.getWriter().printf(resultUuid.toString());
                    return;
                } else {
                    mainOperatingService.edit(declarationTemplate, endDate, logger, securityService.currentUserInfo());
                }

                JSONObject resultUuid = new JSONObject();
                resultUuid.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(logger.getEntries()));
                resp.getWriter().printf(resultUuid.toString());
            } catch (JSONException e) {
                LOG.error(e);
                throw new ServiceException("", e);
            } finally {
                declarationTemplateService.unlock(declarationTemplateId, userInfo);
                IOUtils.closeQuietly(file.getInputStream());
            }
        }
	}

    @RequestMapping(value = "uploadJrxml/{declarationTemplateId}",method = RequestMethod.POST)
    @Transactional
    public void processUpload(@RequestParam(value = "uploader", required = true) MultipartFile file,
                              @PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
            throws FileUploadException, IOException {
        if (checkRole(resp, securityService.currentUserInfo())) {
            if (declarationTemplateId == 0)
                throw new ServiceException("Сначала сохраните шаблон.");

            TAUserInfo userInfo = securityService.currentUserInfo();
            declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
            declarationTemplateService.lock(declarationTemplateId, userInfo);

            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            InputStream inputStream = null;
            try {
                if (file.getSize() == 0)
                    throw new ServiceException("Файл jrxml пустой.");
                if (!file.getOriginalFilename().endsWith(".jrxml"))
                    throw new ServiceException("Формат файла должен быть *.jrxml");
                inputStream = file.getInputStream();
                Date endDate = declarationTemplateService.getDTEndDate(declarationTemplateId);
                Logger logger = new Logger();
                logger.setTaUserInfo(securityService.currentUserInfo());
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationTemplateId);
                if (declarationTemplate.getStatus().equals(VersionedObjectStatus.NORMAL)) {
                    mainOperatingService.isInUsed(
                            declarationTemplateId,
                            declarationTemplate.getType().getId(),
                            declarationTemplate.getStatus(),
                            declarationTemplate.getVersion(),
                            endDate,
                            logger);
                    checkErrors(logger);
                }

                //Проверка на использоваение jrxml другими декларациями
                //http://jira.aplana.com/browse/SBRFACCTAX-12066
                String uploadUuid = blobDataService.create(file.getInputStream(), file.getOriginalFilename());
                declarationTemplate.setCreateScript(declarationTemplateService.getDeclarationTemplateScript(declarationTemplateId));
                if (
                        declarationTemplate.getJrxmlBlobId() != null
                                &&
                                declarationTemplateService.checkExistingDataJrxml(declarationTemplateId, logger)) {
                    JSONObject resultUuid = new JSONObject();
                    resultUuid.put(UuidEnum.ERROR_UUID.toString(), logEntryService.save(logger.getEntries()));
                    resultUuid.put(UuidEnum.UPLOADED_FILE.toString(), uploadUuid);

                    resp.getWriter().printf(resultUuid.toString());
                } else {
                    declarationTemplate.setJrxmlBlobId(uploadUuid);
                    mainOperatingService.edit(declarationTemplate, endDate, logger, securityService.currentUserInfo());

                    checkErrors(logger);

                    JSONObject resultUuid = new JSONObject();
                    resultUuid.put(UuidEnum.UUID.toString(), uploadUuid);
                    resultUuid.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(logger.getEntries()));
                    resp.getWriter().printf(resultUuid.toString());
                }
            } catch (JSONException e) {
                LOG.error(e);
                throw new ServiceException("", e);
            } finally {
                if (inputStream != null) {
                    IOUtils.closeQuietly(inputStream);
                }
                declarationTemplateService.unlock(declarationTemplateId, userInfo);
            }
        }
    }

    @RequestMapping(value = "uploadXsd/{declarationTemplateId}",method = RequestMethod.POST)
    @Transactional
    public void processUploadXsd(@RequestParam(value = "uploader", required = true) MultipartFile file,
                                 @PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
            throws FileUploadException, IOException {
        if (checkRole(resp, securityService.currentUserInfo())) {
            if (declarationTemplateId == 0)
                throw new ServiceException("Сначала сохраните шаблон.");

            if (checkRole(resp, securityService.currentUserInfo())) {
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
                    String xsdBlobId = blobDataService.create(file.getInputStream(), file.getOriginalFilename());
                    declarationTemplate.setXsdId(xsdBlobId);
                    declarationTemplate.setCreateScript(declarationTemplateService.getDeclarationTemplateScript(declarationTemplateId));
                    resultUuid.put(UuidEnum.UUID.toString(), xsdBlobId);
                    declarationTemplateService.save(declarationTemplate);

                    checkErrors(customLog);
                    resultUuid.put(UuidEnum.SUCCESS_UUID.toString(), logEntryService.save(customLog.getEntries()));
                    resp.getWriter().printf(resultUuid.toString());
                } catch (JSONException e) {
                    LOG.error(e);
                    throw new ServiceException("", e);
                } finally {
                    declarationTemplateService.unlock(declarationTemplateId, userInfo);
                }
            }
        }
    }

	@RequestMapping(value = "/downloadByUuid/{uuid}",method = RequestMethod.GET)
	public void processDownload(@PathVariable String uuid, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
        if (checkRole(resp, securityService.currentUserInfo())) {
            BlobData blobData = blobDataService.get(uuid);
            createResponse(req, resp, blobData);
        }
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
		LOG.error(e.getLocalizedMessage(), e);
        JSONObject errors = new JSONObject();
		try {
            Logger log = new Logger();
            log.error(e.getMessage());
            errors.put(UuidEnum.ERROR_UUID.toString(), logEntryService.save(log.getEntries()));
			response.getWriter().printf(errors.toString());
		} catch (IOException ioException) {
			LOG.error(ioException.getMessage(), ioException);
		}
	}

    private void checkErrors(Logger logger) throws IOException {
        if (logger.containsLevel(LogLevel.ERROR)){
           throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
    }

    private void createResponse(final HttpServletRequest req, final HttpServletResponse response, final BlobData blobData) throws IOException{
        String fileName = blobData.getName();
        setCorrectFileName(req, response, fileName);

        DataInputStream in = new DataInputStream(blobData.getInputStream());
        OutputStream out = response.getOutputStream();
        int count = 0;
        try {
            count = IOUtils.copy(in, out);
        } finally {
            in.close();
            out.close();
        }
        response.setContentLength(count);
    }

    private void setCorrectFileName(HttpServletRequest request, HttpServletResponse response, String originalFileName) throws UnsupportedEncodingException {
        String userAgent = request.getHeader("User-Agent").toLowerCase();
        String fileName = URLEncoder.encode(originalFileName, ENCODING).replaceAll("\\+", "%20");
        String fileNameAttr = "filename=";
        if (userAgent.contains("msie") || userAgent.contains("webkit")) {
            fileName = "\"" + fileName + "\"";
        } else {
            fileNameAttr = fileNameAttr.replace("=", "*=") + ENCODING + "''";
        }
        response.setHeader("Content-Disposition", "attachment;" + fileNameAttr + fileName);
    }

    private boolean checkRole(HttpServletResponse response, TAUserInfo userInfo) throws IOException {
        if (!userInfo.getUser().hasRole(TARole.ROLE_CONF)) {
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().printf("Ошибка доступа (недостаточно прав)");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}