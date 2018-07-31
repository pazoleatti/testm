package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * Контроллер для работы с шаблоном декларации
 * TODO только для gwt!!! ошибки обрабатываются через свой @ExceptionHandler, поправится после перехода настройщика на angular
 */
@RestController
public class DeclarationTemplateControllerGWT {

    private static final Log LOG = LogFactory.getLog(DeclarationTemplateControllerGWT.class);

    private SecurityService securityService;
    private DeclarationTemplateService declarationTemplateService;
    private final MainOperatingService mainOperatingService;
    private BlobDataService blobDataService;
    private LogEntryService logEntryService;

    public DeclarationTemplateControllerGWT(SecurityService securityService, DeclarationTemplateService declarationTemplateService,
                                            @Qualifier("declarationTemplateMainOperatingService") MainOperatingService mainOperatingService, BlobDataService blobDataService,
                                            LogEntryService logEntryService) {
        this.securityService = securityService;
        this.declarationTemplateService = declarationTemplateService;
        this.mainOperatingService = mainOperatingService;
        this.blobDataService = blobDataService;
        this.logEntryService = logEntryService;
    }

    /**
     * Загрузка файла в формате jrxml
     *
     * @param file                  файл шаблона декларации
     * @param declarationTemplateId идентификатор шаблона декларации
     * @param req                   запрос
     * @param resp                  ответ
     * @throws IOException IOException
     */
    @PostMapping(value = "/actions/declarationTemplate/{declarationTemplateId}/uploadJrxml")
    @Transactional
    public void processUpload(@RequestParam(value = "uploader") MultipartFile file,
                              @PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (checkRole(resp, securityService.currentUserInfo())) {
            if (declarationTemplateId == 0)
                throw new ServiceException("Сначала сохраните шаблон.");

            TAUserInfo userInfo = securityService.currentUserInfo();
            declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
            declarationTemplateService.lock(declarationTemplateId, userInfo);

            req.setCharacterEncoding(UTF_8);
            resp.setCharacterEncoding(UTF_8);
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

    /**
     * Загрузка файла в формате xsd
     *
     * @param file                  файл шаблона декларации
     * @param declarationTemplateId идентификатор шаблона декларации
     * @param req                   запрос
     * @param resp                  ответ
     * @throws IOException IOException
     */
    @PostMapping(value = "/actions/declarationTemplate/{declarationTemplateId}/uploadXsd")
    @Transactional
    public void processUploadXsd(@RequestParam(value = "uploader") MultipartFile file,
                                 @PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (checkRole(resp, securityService.currentUserInfo())) {
            if (declarationTemplateId == 0)
                throw new ServiceException("Сначала сохраните шаблон.");

            if (checkRole(resp, securityService.currentUserInfo())) {
                TAUserInfo userInfo = securityService.currentUserInfo();
                declarationTemplateService.checkLockedByAnotherUser(declarationTemplateId, userInfo);
                declarationTemplateService.lock(declarationTemplateId, userInfo);

                req.setCharacterEncoding(UTF_8);
                resp.setCharacterEncoding(UTF_8);
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
                    declarationTemplateService.save(declarationTemplate, userInfo);

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

    /**
     * Проверка logger на наличие ошибок
     *
     * @param logger логгер
     */
    private void checkErrors(Logger logger) {
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("", logEntryService.save(logger.getEntries()));
        }
    }

    /**
     * Проверка пользователя на наличие необходимых прав
     *
     * @param response ответ
     * @param userInfo данные пользователя
     * @return признак наличия прав
     * @throws IOException IOException
     */
    private boolean checkRole(HttpServletResponse response, TAUserInfo userInfo) throws IOException {
        if (!userInfo.getUser().hasRole(TARole.N_ROLE_CONF)) {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setCharacterEncoding(UTF_8);
            response.getWriter().printf("Ошибка доступа (недостаточно прав)");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
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
}