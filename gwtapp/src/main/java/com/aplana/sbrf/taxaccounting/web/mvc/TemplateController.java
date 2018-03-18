package com.aplana.sbrf.taxaccounting.web.mvc;

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
    public JqgridPagedList<DeclarationType> fetchDeclarations() {
        List<DeclarationType> types = declarationTypeService.fetchAll();

        return JqgridPagedResourceAssembler.buildPagedList(
                types,
                types.size(),
                PagingParams.getInstance(1, types.size())
        );
    }
}
