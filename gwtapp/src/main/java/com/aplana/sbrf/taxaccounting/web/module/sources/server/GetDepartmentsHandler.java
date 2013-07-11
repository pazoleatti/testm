package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDepartmentsHandler extends AbstractActionHandler<GetDepartmentsAction, GetDepartmentsResult> {

	@Autowired
	private DepartmentService departmentService;

    public GetDepartmentsHandler() {
        super(GetDepartmentsAction.class);
    }

    @Override
    public GetDepartmentsResult execute(GetDepartmentsAction action, ExecutionContext context) throws ActionException {
		GetDepartmentsResult result = new GetDepartmentsResult();
		result.setDepartments(departmentService.listDepartments());
		return result;
    }

    @Override
    public void undo(GetDepartmentsAction action, GetDepartmentsResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
