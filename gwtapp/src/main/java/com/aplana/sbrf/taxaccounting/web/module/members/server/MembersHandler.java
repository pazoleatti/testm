package com.aplana.sbrf.taxaccounting.web.module.members.server;

import com.aplana.sbrf.taxaccounting.model.*;
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
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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

        PagingResult<TAUserView> taUserFulls = taUserService.getUsersByFilter(action.getMembersFilterData());
	    result.setTaUserList(taUserFulls);
	    result.setStartIndex(action.getMembersFilterData().getStartIndex());
        return result;
    }

    @Override
    public void undo(GetMembersAction action, GetMembersResult result, ExecutionContext context) throws ActionException {
    }
}
