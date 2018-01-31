package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTypeAssignment;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.EditFormResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.EditFormsAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author auldanov
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class EditFormHandler extends AbstractActionHandler<EditFormsAction, EditFormResult> {

    @Autowired
    private SourceService departmentFormTypeService;

    public EditFormHandler() {
        super(EditFormsAction.class);
    }

    @Override
    public EditFormResult execute(EditFormsAction action, ExecutionContext executionContext) throws ActionException {
        for (DeclarationTypeAssignment f : action.getDeclarationTypeAssignments()) {
            departmentFormTypeService.updateDDTPerformers(f.getId().intValue(), action.getPerformers());
        }
        return null;
    }

    @Override
    public void undo(EditFormsAction action, EditFormResult editFormResult, ExecutionContext executionContext) throws ActionException {

    }
}
