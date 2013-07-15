package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateFormSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateFormSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class UpdateFormSourcesHandler extends AbstractActionHandler<UpdateFormSourcesAction, UpdateFormSourcesResult> {

	@Autowired
	private DepartmentFormTypeService departmentFormTypeService;

    public UpdateFormSourcesHandler() {
        super(UpdateFormSourcesAction.class);
    }

    @Override
    public UpdateFormSourcesResult execute(UpdateFormSourcesAction action, ExecutionContext context) {
		departmentFormTypeService.saveFormSources(action.getDepartmentFormTypeId(), action.getSourceDepartmentFormTypeIds());
		return new UpdateFormSourcesResult();
    }

    @Override
    public void undo(UpdateFormSourcesAction action, UpdateFormSourcesResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
