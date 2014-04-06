package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDTVersionChangesAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDTVersionChangesResult;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.TemplateChangesExt;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetDTVersionChangesHandler extends AbstractActionHandler<GetDTVersionChangesAction, GetDTVersionChangesResult> {
    @Autowired
    private TemplateChangesService templateChangesService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public GetDTVersionChangesHandler() {
        super(GetDTVersionChangesAction.class);
    }

    @Override
    public GetDTVersionChangesResult execute(GetDTVersionChangesAction action, ExecutionContext executionContext) throws ActionException {
        GetDTVersionChangesResult result = new GetDTVersionChangesResult();
        List<TemplateChanges> changeses = templateChangesService.getByDeclarationTemplateId(action.getDeclarationTemplateId());
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>(changeses.size());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(action.getDeclarationTemplateId());
        for (TemplateChanges changes : changeses){
            TemplateChangesExt templateChangesExt = new TemplateChangesExt();
            templateChangesExt.setTemplateChanges(changes);
            templateChangesExt.setEdition(declarationTemplate.getEdition());
            changesList.add(templateChangesExt);
        }

        result.setChanges(changesList);
        return result;
    }

    @Override
    public void undo(GetDTVersionChangesAction getDTVersionChangesAction, GetDTVersionChangesResult getDTVersionChangesResult, ExecutionContext executionContext) throws ActionException {
    }
}
