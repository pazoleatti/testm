package com.aplana.sbrf.taxaccounting.web.module.userlist.server;

import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.userlist.shared.GetUserListAction;
import com.aplana.sbrf.taxaccounting.web.module.userlist.shared.GetUserListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * User: avanteev
 * Date: 2013
 */

@Component
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserListHandler extends AbstractActionHandler<GetUserListAction, GetUserListResult> {

    @Autowired
    TAUserService taUserService;

    public UserListHandler() {
        super(GetUserListAction.class);
    }

    @Override
    public GetUserListResult execute(GetUserListAction action, ExecutionContext context) throws ActionException {
        GetUserListResult result = new GetUserListResult();
        result.setTaUserList(taUserService.lisAllFullActiveUsers());
        return result;
    }

    @Override
    public void undo(GetUserListAction action, GetUserListResult result, ExecutionContext context) throws ActionException {

    }
}
