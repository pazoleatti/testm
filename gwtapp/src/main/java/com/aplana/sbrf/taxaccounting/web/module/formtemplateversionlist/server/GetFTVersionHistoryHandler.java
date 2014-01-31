package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.GetFTVersionHistoryAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.GetFTVersionHistoryResult;
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
public class GetFTVersionHistoryHandler extends AbstractActionHandler<GetFTVersionHistoryAction, GetFTVersionHistoryResult> {

    @Autowired
    private TemplateChangesService templateChangesService;

    @Autowired
    private FormTemplateService formTemplateService;

    public GetFTVersionHistoryHandler() {
        super(GetFTVersionHistoryAction.class);
    }

    @Override
    public GetFTVersionHistoryResult execute(GetFTVersionHistoryAction action, ExecutionContext context) throws ActionException {
        List<FormTemplate> formTemplates = formTemplateService.getFormTemplateVersionsByStatus(action.getFormTypeId(),
                VersionedObjectStatus.DRAFT, VersionedObjectStatus.NORMAL, VersionedObjectStatus.DELETED);
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>();
        for (FormTemplate formTemplate : formTemplates) {
            //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
            for (TemplateChanges changes : templateChangesService.getByFormTemplateId(formTemplate.getId())){
                TemplateChangesExt templateChangesExt = new TemplateChangesExt();
                templateChangesExt.setTemplateChanges(changes);
                templateChangesExt.setEdition(formTemplate.getEdition());
                changesList.add(templateChangesExt);
            }
        }
        GetFTVersionHistoryResult result = new GetFTVersionHistoryResult();
        result.setChangeses(changesList);
        return result;
    }

    @Override
    public void undo(GetFTVersionHistoryAction action, GetFTVersionHistoryResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
