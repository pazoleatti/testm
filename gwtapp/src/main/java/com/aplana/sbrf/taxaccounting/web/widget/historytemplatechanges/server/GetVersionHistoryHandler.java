package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.TemplateChangesExt;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetVersionHistoryAction;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetVersionHistoryResult;
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
public class GetVersionHistoryHandler extends AbstractActionHandler<GetVersionHistoryAction, GetVersionHistoryResult> {
    @Autowired
    private TemplateChangesService templateChangesService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public GetVersionHistoryHandler() {
        super(GetVersionHistoryAction.class);
    }

    @Override
    public GetVersionHistoryResult execute(GetVersionHistoryAction action, ExecutionContext context) throws ActionException {
        GetVersionHistoryResult result = new GetVersionHistoryResult();
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>();
        switch (action.getTemplateType()){
            case DECLARATION:
                List<FormTemplate> formTemplates = formTemplateService.getFormTemplateVersionsByStatus(action.getTypeId(),
                        VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL);

                for (FormTemplate formTemplate : formTemplates) {
                    for (TemplateChanges changes : templateChangesService.getByFormTemplateId(formTemplate.getId())){
                        TemplateChangesExt templateChangesExt = new TemplateChangesExt();
                        templateChangesExt.setTemplateChanges(changes);
                        templateChangesExt.setEdition(formTemplate.getEdition());
                        changesList.add(templateChangesExt);
                    }
                }
                break;
            case FORM:
                List<DeclarationTemplate> declarationTemplates = declarationTemplateService.getDecTemplateVersionsByStatus(action.getTypeId(),
                        VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL);
                for (DeclarationTemplate decTemplate : declarationTemplates) {
                    for (TemplateChanges changes : templateChangesService.getByDeclarationTemplateId(decTemplate.getId())){
                        TemplateChangesExt templateChangesExt = new TemplateChangesExt();
                        templateChangesExt.setTemplateChanges(changes);
                        templateChangesExt.setEdition(decTemplate.getEdition());
                        changesList.add(templateChangesExt);
                    }
                }
                break;

        }

        result.setChangeses(changesList);
        return result;
    }

    @Override
    public void undo(GetVersionHistoryAction action, GetVersionHistoryResult result, ExecutionContext context) throws ActionException {

    }
}
