package com.aplana.sbrf.taxaccounting.web.module.members.server;

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
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class MembersHandler extends AbstractActionHandler<GetMembersAction, GetMembersResult> {

    @Autowired
    TAUserService taUserService;

    public MembersHandler() {
        super(GetMembersAction.class);
    }

    @Override
    public GetMembersResult execute(GetMembersAction action, ExecutionContext context) throws ActionException {
        GetMembersResult result = new GetMembersResult();
	    result.setTaUserList(taUserService.getByFilter(action.getMembersFilterData()));
		result.setStartIndex(action.getMembersFilterData().getStartIndex());
        return result;
    }

    @Override
    public void undo(GetMembersAction action, GetMembersResult result, ExecutionContext context) throws ActionException {

    }
}
