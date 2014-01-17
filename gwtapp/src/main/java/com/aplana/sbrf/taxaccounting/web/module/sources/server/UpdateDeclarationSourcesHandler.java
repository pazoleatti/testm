package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateDeclarationSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class UpdateDeclarationSourcesHandler extends AbstractActionHandler<UpdateDeclarationSourcesAction,
		UpdateSourcesResult> {

	@Autowired
	private SourceService departmentFormTypeService;

    public UpdateDeclarationSourcesHandler() {
        super(UpdateDeclarationSourcesAction.class);
    }

    @Override
    public UpdateSourcesResult execute(UpdateDeclarationSourcesAction action, ExecutionContext context) {
		departmentFormTypeService.saveDeclarationSources(Long.valueOf(action.getDepartmentDeclarationTypeId()),
				action.getSourceDepartmentFormTypeIds());
		return new UpdateSourcesResult();
    }

    @Override
    public void undo(UpdateDeclarationSourcesAction action, UpdateSourcesResult result, ExecutionContext context)
			throws ActionException {
        // Nothing!
    }
}
