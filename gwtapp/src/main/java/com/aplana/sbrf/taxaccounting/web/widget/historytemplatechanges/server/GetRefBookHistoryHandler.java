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
 * @author lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetRefBookHistoryHandler extends AbstractActionHandler<GetRefBookHistoryAction, GetRefBookHistoryResult> {

    @Autowired
    private TemplateChangesService templateChangesService;

    public GetRefBookHistoryHandler() {
        super(GetRefBookHistoryAction.class);
    }

    @Override
    public GetRefBookHistoryResult execute(GetRefBookHistoryAction action, ExecutionContext context) throws ActionException {
        List<TemplateChanges> changeses = templateChangesService.getByRefBookIds(action.getTypeId(),
                action.getSortFilter().getSearchOrdering(), action.getSortFilter().isAscSorting());
        List<TemplateChangesExt> changesList = new ArrayList<TemplateChangesExt>(changeses.size());
        for (TemplateChanges changes : changeses) {
            TemplateChangesExt templateChangesExt = new TemplateChangesExt();
            templateChangesExt.setTemplateChanges(changes);
            changesList.add(templateChangesExt);
        }
        GetRefBookHistoryResult result = new GetRefBookHistoryResult();
        result.setChangesExtList(changesList);
        return result;
    }

    @Override
    public void undo(GetRefBookHistoryAction action, GetRefBookHistoryResult result, ExecutionContext context) throws ActionException {
    }
}
