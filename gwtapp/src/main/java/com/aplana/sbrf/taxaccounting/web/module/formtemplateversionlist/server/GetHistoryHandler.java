package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.GetVersionHistoryAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.GetVersionHistoryResult;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.TemplateChangesExt;
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
public class GetHistoryHandler extends AbstractActionHandler<GetVersionHistoryAction, GetVersionHistoryResult> {

    @Autowired
    private TemplateChangesService templateChangesService;

    @Autowired
    private FormTemplateService formTemplateService;

    public GetHistoryHandler() {
        super(GetVersionHistoryAction.class);
    }

    @Override
    public GetVersionHistoryResult execute(GetVersionHistoryAction action, ExecutionContext context) throws ActionException {
        List<FormTemplate> formTemplates = formTemplateService.getFormTemplateVersionsByStatus(action.getFormTypeId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL);
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>();
        for (FormTemplate formTemplate : formTemplates) {
            for (TemplateChanges changes : templateChangesService.getByFormTemplateId(formTemplate.getId())){
                TemplateChangesExt templateChangesExt = new TemplateChangesExt();
                templateChangesExt.setTemplateChanges(changes);
                templateChangesExt.setEdition(formTemplate.getEdition());
                changesList.add(templateChangesExt);
            }
        }
        GetVersionHistoryResult result = new GetVersionHistoryResult();
        result.setChangeses(changesList);
        return result;
    }

    @Override
    public void undo(GetVersionHistoryAction action, GetVersionHistoryResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
