package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.server;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetDTHistoryAction;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetDTHistoryResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetDTHistoryHandler extends AbstractActionHandler<GetDTHistoryAction, GetDTHistoryResult> {

    @Autowired
    private TemplateChangesService templateChangesService;

    public GetDTHistoryHandler() {
        super(GetDTHistoryAction.class);
    }

    @Override
    public GetDTHistoryResult execute(GetDTHistoryAction action, ExecutionContext executionContext) throws ActionException {
        List<TemplateChanges> changeses = templateChangesService.getByDeclarationTypeIds(action.getTypeId(),
                action.getSortFilter().getSearchOrdering(), action.getSortFilter().isAscSorting());
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>(changeses.size());
        for (TemplateChanges changes : changeses){
            TemplateChangesExt templateChangesExt = new TemplateChangesExt();
            templateChangesExt.setTemplateChanges(changes);
            changesList.add(templateChangesExt);
        }
        GetDTHistoryResult result = new GetDTHistoryResult();
        result.setChangesExtList(changesList);
        return result;
    }

    @Override
    public void undo(GetDTHistoryAction getDTHistoryAction, GetDTHistoryResult getDTHistoryResult, ExecutionContext executionContext) throws ActionException {
    }
}
