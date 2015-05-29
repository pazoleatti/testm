package com.aplana.sbrf.taxaccounting.web.module.lock.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.StopAsyncAction;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.StopAsyncResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Удаление блокировок
 * @author dloshkarev
 */
@Service
public class StopAsyncHandler extends AbstractActionHandler<StopAsyncAction, StopAsyncResult> {

    @Autowired
    LockDataService lockService;

    @Autowired
    SecurityService securityService;

    public StopAsyncHandler() {
        super(StopAsyncAction.class);
    }

    @Override
    public StopAsyncResult execute(StopAsyncAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        lockService.interuptAllTasks(action.getKeys(), userInfo.getUser().getId());
        return new StopAsyncResult();
    }

    @Override
    public void undo(StopAsyncAction action, StopAsyncResult result, ExecutionContext context) throws ActionException {

    }
}
