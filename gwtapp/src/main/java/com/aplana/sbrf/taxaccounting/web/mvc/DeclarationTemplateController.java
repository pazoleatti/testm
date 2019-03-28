package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.UpdateTemplateAction;
import com.aplana.sbrf.taxaccounting.model.action.UpdateTemplateStatusAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.UpdateTemplateResult;
import com.aplana.sbrf.taxaccounting.model.result.UpdateTemplateStatusResult;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.model.CustomMediaType;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@RestController
public class DeclarationTemplateController {

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private SecurityService securityService;

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
        binder.registerCustomEditor(UpdateTemplateAction.class, new RequestParamEditor(UpdateTemplateAction.class));
        binder.registerCustomEditor(UpdateTemplateStatusAction.class, new RequestParamEditor(UpdateTemplateStatusAction.class));
    }

    /**
     * Возвращяет список типов налоговых форм
     */
    @GetMapping(value = "/rest/declarationType", params = "projection=declarationTypeJournal")
    public List<DeclarationType> fetchDeclarationTypes() {
        return declarationTypeService.fetchAll(securityService.currentUserInfo());
    }

    /**
     * Возвращяет тип макета по идентификатору
     */
    @GetMapping(value = "/rest/declarationType")
    public DeclarationType fetchDeclarationType(@RequestParam int declarationTypeId) {
        return declarationTypeService.get(declarationTypeId);
    }

    /**
     * Возвращяет список макетов налоговых форм по типу макета
     */
    @GetMapping(value = "/rest/declarationTemplate/{typeId}", params = "projection=allByTypeId")
    public List<DeclarationTemplate> fetchDeclarationTemplatesByTypeId(@PathVariable int typeId) {
        return declarationTemplateService.fetchAllByType(typeId, securityService.currentUserInfo());
    }

    /**
     * Возвращяет макет по идентификатору
     */
    @GetMapping(value = "/rest/declarationTemplate/{id}", params = "projection=fetchOne")
    public DeclarationTemplate fetchDeclarationTemplate(@PathVariable int id) {
        return declarationTemplateService.fetchWithScripts(id);
    }

    /**
     * Возвращяет данные о фатальности проверок по макету
     */
    @GetMapping(value = "/rest/declarationTemplate", params = "projection=fetchChecks")
    public List<DeclarationTemplateCheck> fetchChecksByTemplateId(@RequestParam int declarationTypeId,
                                                                  @RequestParam(required = false) Integer declarationTemplateId) {
        return declarationTemplateService.getChecks(declarationTypeId, declarationTemplateId);
    }

    /**
     * Изменяет данные по макету
     */
    @PostMapping(value = "/rest/declarationTemplate", params = "projection=updateTemplate")
    public UpdateTemplateResult updateTemplate(@RequestBody UpdateTemplateAction action) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationTemplateService.update(action, userInfo);
    }

    /**
     * Вводит/Выводит макет из действия
     */
    @PostMapping(value = "/rest/declarationTemplate", params = "projection=updateStatus")
    public UpdateTemplateStatusResult updateStatus(@RequestBody UpdateTemplateStatusAction action) {
        TAUserInfo userInfo = securityService.currentUserInfo();
        return declarationTemplateService.updateStatus(action, userInfo);
    }

    /**
     * Загрузка xsd макета
     */
    @PostMapping(value = "/actions/declarationTemplate/uploadXsd", produces = MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
    public String uploadXsd(@RequestParam int declarationTemplateId, @RequestParam("uploader") MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return declarationTemplateService.uploadXsd(declarationTemplateId, inputStream, file.getOriginalFilename());
        }
    }

    /**
     * Выгрузить xsd шаблона
     * @param declarationTemplateId идентификатор макета
     * @param req   запрос
     * @param resp  ответ
     * @throws IOException IOException
     */
    @GetMapping(value="/actions/declarationTemplate/{declarationTemplateId}", params = "projection=downloadXsd")
    public void downloadXsd(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BlobData blobData = declarationTemplateService.downloadXsd(declarationTemplateId);
        if (blobData != null) {
            ResponseUtils.createBlobResponse(req, resp, blobData);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Выгрузка архива с содержимым макета декларации
     *
     * @param declarationTemplateId идентификатор макета декларации
     * @param resp                  ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/rest/declarationTemplate/{declarationTemplateId}", params = "projection=export")
    public void exportDeclarationTemplate(@PathVariable int declarationTemplateId, HttpServletResponse resp)
            throws IOException {
        String fileName = "declarationTemplate_" + declarationTemplateId + ".zip";
        resp.setContentType(CustomMediaType.APPLICATION_ZIP_VALUE);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            declarationTemplateService.exportDeclarationTemplate(securityService.currentUserInfo(), declarationTemplateId, resp.getOutputStream());
        } finally {
            IOUtils.closeQuietly(resp.getOutputStream());
        }
    }

    /**
     * Выгрузка архива с содержимым всех макетов деклараций
     *
     * @param resp                  ответ
     * @throws IOException IOException
     */
    @GetMapping(value = "/actions/declarationTemplate/downloadAll")
    public void exportAllDeclarationTemplates(HttpServletResponse resp) throws IOException {
        String fileName = String.format("Templates_%s.zip", new SimpleDateFormat("yyyy_MM_dd").format(new Date()));
        resp.setContentType(CustomMediaType.APPLICATION_ZIP_VALUE);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            declarationTemplateService.exportAllDeclarationTemplates(securityService.currentUserInfo(), resp.getOutputStream());
        } finally {
            IOUtils.closeQuietly(resp.getOutputStream());
        }
    }

    /**
     * Загрузка архива в макет декларации
     *
     * @param file                  архив с содержимым макета
     * @param declarationTemplateId идентификатор макета декларации
     * @throws IOException IOException
     */
    @ResponseBody
    @PostMapping(value = "/rest/declarationTemplate/{declarationTemplateId}", params = "projection=import")
    public ActionResult importDeclarationTemplate(@RequestParam(value = "uploader") MultipartFile file,
                                                  @PathVariable int declarationTemplateId) throws IOException {
        if (declarationTemplateId == 0)
            throw new ServiceException("Сначала сохраните шаблон.");
        if (file.getSize() == 0)
            throw new ServiceException("Архив пустой.");

        return declarationTemplateService.importDeclarationTemplate(securityService.currentUserInfo(),
                declarationTemplateId, file.getInputStream());
    }

    /**
     * Удаляет отчеты форм, связанные с jrxml макета
     */
    @PostMapping(value = "/rest/declarationTemplate/{declarationTemplateId}", params = "projection=deleteJrxmlReports")
    public void deleteJrxmlReports(@PathVariable int declarationTemplateId) {
        declarationTemplateService.deleteJrxmlReports(securityService.currentUserInfo(), declarationTemplateId);
    }
}
