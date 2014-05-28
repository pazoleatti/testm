package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateCurrentAssignsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateCurrentAssignsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class UpdateCurrentAssignsHandler extends AbstractActionHandler<UpdateCurrentAssignsAction, UpdateCurrentAssignsResult> {

	@Autowired
	private SourceService departmentFormTypeService;

    public UpdateCurrentAssignsHandler() {
        super(UpdateCurrentAssignsAction.class);
    }

    @Override
    public UpdateCurrentAssignsResult execute(UpdateCurrentAssignsAction action, ExecutionContext context) {
        if(action.isForm()){
            departmentFormTypeService.saveFormSources(action.getDepartmentAssignId(), action.getRightDepartmentAssignIds());
        } else {
            departmentFormTypeService.saveDeclarationSources(action.getDepartmentAssignId(), action.getRightDepartmentAssignIds());
        }

		return new UpdateCurrentAssignsResult();
    }

    @Override
    public void undo(UpdateCurrentAssignsAction action, UpdateCurrentAssignsResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
