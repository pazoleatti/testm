package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.server;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetFTVersionHistoryAction;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetFTVersionHistoryResult;
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
@PreAuthorize("hasRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetFTVersionHistoryHandler extends AbstractActionHandler<GetFTVersionHistoryAction, GetFTVersionHistoryResult> {

    @Autowired
    private TemplateChangesService templateChangesService;

    public GetFTVersionHistoryHandler() {
        super(GetFTVersionHistoryAction.class);
    }

    @Override
    public GetFTVersionHistoryResult execute(GetFTVersionHistoryAction action, ExecutionContext executionContext) throws ActionException {
        GetFTVersionHistoryResult result = new GetFTVersionHistoryResult();
        List<TemplateChanges> changeses = templateChangesService.getByFormTemplateId(action.getTemplateId(),
                action.getSortFilter().getSearchOrdering(), action.getSortFilter().isAscSorting());
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>(changeses.size());
        for (TemplateChanges changes : changeses){
            TemplateChangesExt templateChangesExt = new TemplateChangesExt();
            templateChangesExt.setTemplateChanges(changes);
            changesList.add(templateChangesExt);
        }

        result.setChangesExtList(changesList);
        return result;
    }

    @Override
    public void undo(GetFTVersionHistoryAction getVersionHistoryAction, GetFTVersionHistoryResult getVersionHistoryResult, ExecutionContext executionContext) throws ActionException {
        //Nothing
    }
}
