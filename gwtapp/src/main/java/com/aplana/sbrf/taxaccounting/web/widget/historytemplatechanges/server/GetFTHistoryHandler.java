package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.server;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.*;
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
public class GetFTHistoryHandler extends AbstractActionHandler<GetFTHistoryAction, GetFTHistoryResult> {

    @Autowired
    private TemplateChangesService templateChangesService;

    public GetFTHistoryHandler() {
        super(GetFTHistoryAction.class);
    }

    @Override
    public GetFTHistoryResult execute(GetFTHistoryAction action, ExecutionContext context) throws ActionException {
        List<TemplateChanges> changeses = templateChangesService.getByFormTypeIds(action.getTypeId(),
                action.getSortFilter().getSearchOrdering(), action.getSortFilter().isAscSorting());
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>(changeses.size());
        for (TemplateChanges changes : changeses) {
            TemplateChangesExt templateChangesExt = new TemplateChangesExt();
            templateChangesExt.setTemplateChanges(changes);
            changesList.add(templateChangesExt);
        }
        GetFTHistoryResult result = new GetFTHistoryResult();
        result.setChangesExtList(changesList);
        return result;
    }

    @Override
    public void undo(GetFTHistoryAction action, GetFTHistoryResult result, ExecutionContext context) throws ActionException {
    }
}
