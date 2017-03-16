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
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetDTVersionHistoryHandler extends AbstractActionHandler<GetDTVersionHistoryAction, GetDTVersionHistoryResult> {
    @Autowired
    private TemplateChangesService templateChangesService;

    public GetDTVersionHistoryHandler() {
        super(GetDTVersionHistoryAction.class);
    }

    @Override
    public GetDTVersionHistoryResult execute(GetDTVersionHistoryAction action, ExecutionContext executionContext) throws ActionException {
        GetDTVersionHistoryResult result = new GetDTVersionHistoryResult();
        List<TemplateChanges> changeses = templateChangesService.getByDeclarationTemplateId(action.getTemplateId(),
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
    public void undo(GetDTVersionHistoryAction getDTVersionChangesAction, GetDTVersionHistoryResult getDTVersionChangesResult, ExecutionContext executionContext) throws ActionException {
    }
}
