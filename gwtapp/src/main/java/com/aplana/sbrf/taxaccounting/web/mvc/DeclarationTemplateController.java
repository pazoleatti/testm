package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateCheck;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.UpdateTemplateStatusAction;
import com.aplana.sbrf.taxaccounting.model.action.UpdateTemplateAction;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.model.result.UpdateTemplateStatusResult;
import com.aplana.sbrf.taxaccounting.model.result.UpdateTemplateResult;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return declarationTypeService.fetchAll();
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
        return declarationTemplateService.fetchAllByType(typeId);
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

}
