package com.aplana.sbrf.taxaccounting.web.module.members.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserFull;
import com.aplana.sbrf.taxaccounting.model.TAUserFullWithDepartmentPath;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.GetMembersAction;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.GetMembersResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * User: Eugene Stetsenko
 * Date: 2013
 */

@Component
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_OPER', 'ROLE_CONTROL_NS')")
public class MembersHandler extends AbstractActionHandler<GetMembersAction, GetMembersResult> {

    @Autowired
    TAUserService taUserService;
	@Autowired
	DepartmentService departmentService;

    public MembersHandler() {
        super(GetMembersAction.class);
    }

    @Override
    public GetMembersResult execute(GetMembersAction action, ExecutionContext context) throws ActionException {
        GetMembersResult result = new GetMembersResult();
		result.setStartIndex(action.getMembersFilterData().getStartIndex());

	    PagingResult<TAUserFullWithDepartmentPath> page = new PagingResult<TAUserFullWithDepartmentPath>();
	    for (TAUserFull user : taUserService.getByFilter(action.getMembersFilterData())) {
		    TAUserFullWithDepartmentPath fullUser = new TAUserFullWithDepartmentPath();
		    fullUser.setDepartment(user.getDepartment());
		    fullUser.setUser(user.getUser());
		    String fullDepartment = getFullDepartment(user.getDepartment());
		    fullDepartment = fullDepartment.substring(1);
		    fullUser.setFullDepartmentPath(fullDepartment);
		    page.add(fullUser);
	    }

	    result.setTaUserList(page);
        return result;
    }

    @Override
    public void undo(GetMembersAction action, GetMembersResult result, ExecutionContext context) throws ActionException {
    }

	String getFullDepartment(Department department) {

		StringBuilder fullDepartment = new StringBuilder();
		fullDepartment.insert(0, "/" + department.getName());
		if (department.getParentId() != null) {
			fullDepartment.insert(0, getFullDepartment(departmentService.getDepartment(department.getParentId())));
		}
		return fullDepartment.toString();
	}
}
