package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.filter.RequestParamEditor;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedList;
import com.aplana.sbrf.taxaccounting.web.paging.JqgridPagedResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class TemplateController {

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    @Autowired
    private DeclarationTypeService declarationTypeService;

    @InitBinder
    public void init(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(PagingParams.class, new RequestParamEditor(PagingParams.class));
    }

    /**
     * Возвращяет список типов налоговых форм
     */
    @GetMapping(value = "/rest/declarationType", params = "projection=declarationTypeJournal")
    public JqgridPagedList<DeclarationType> fetchDeclarationTypes() {
        List<DeclarationType> types = declarationTypeService.fetchAll();

        return JqgridPagedResourceAssembler.buildPagedList(
                types,
                types.size(),
                PagingParams.getInstance(1, types.size())
        );
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
    @GetMapping(value = "/rest/declarationTemplate/{id}", params = "projection=allByTypeId")
    public JqgridPagedList<DeclarationTemplate> fetchDeclarationTemplatesByTypeId(@PathVariable int id) {
        List<DeclarationTemplate> types = declarationTemplateService.fetchAllByType(id);

        return JqgridPagedResourceAssembler.buildPagedList(
                types,
                types.size(),
                PagingParams.getInstance(1, types.size())
        );
    }

    /**
     * Возвращяет макет по идентификатору
     */
    @GetMapping(value = "/rest/declarationTemplate/{id}", params = "projection=fetchOne")
    public DeclarationTemplate fetchDeclarationTemplate(@PathVariable int id) {
        return declarationTemplateService.get(id);
    }
}
