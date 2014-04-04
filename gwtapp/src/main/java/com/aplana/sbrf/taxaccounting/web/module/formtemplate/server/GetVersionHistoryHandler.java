package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetVersionHistoryAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetVersionHistoryResult;
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
public class GetVersionHistoryHandler extends AbstractActionHandler<GetVersionHistoryAction, GetVersionHistoryResult> {

    @Autowired
    private TemplateChangesService templateChangesService;
    @Autowired
    private FormTemplateService formTemplateService;

    public GetVersionHistoryHandler() {
        super(GetVersionHistoryAction.class);
    }

    @Override
    public GetVersionHistoryResult execute(GetVersionHistoryAction action, ExecutionContext executionContext) throws ActionException {
        GetVersionHistoryResult result = new GetVersionHistoryResult();
        List<TemplateChanges> changeses = templateChangesService.getByFormTemplateId(action.getFormTemplateId());
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>(changeses.size());
        FormTemplate formTemplate = formTemplateService.get(action.getFormTemplateId());
        for (TemplateChanges changes : changeses){
            TemplateChangesExt templateChangesExt = new TemplateChangesExt();
            templateChangesExt.setTemplateChanges(changes);
            templateChangesExt.setEdition(formTemplate.getEdition());
            changesList.add(templateChangesExt);
        }

        result.setChanges(changesList);
        return result;
    }

    @Override
    public void undo(GetVersionHistoryAction getVersionHistoryAction, GetVersionHistoryResult getVersionHistoryResult, ExecutionContext executionContext) throws ActionException {
        //Nothing
    }
}
