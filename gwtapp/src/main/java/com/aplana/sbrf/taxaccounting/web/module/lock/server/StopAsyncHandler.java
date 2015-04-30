package com.aplana.sbrf.taxaccounting.web.module.lock.server;

import com.aplana.sbrf.taxaccounting.async.manager.AsyncInterruptionManager;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
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
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP')")
public class StopAsyncHandler extends AbstractActionHandler<StopAsyncAction, StopAsyncResult> {

    @Autowired
    AsyncInterruptionManager asyncInterruptionManager;

    @Autowired
    LockDataService lockService;

    public StopAsyncHandler() {
        super(StopAsyncAction.class);
    }

    @Override
    public StopAsyncResult execute(StopAsyncAction action, ExecutionContext context) throws ActionException {
        asyncInterruptionManager.interruptAll();
        lockService.unlockAll(action.getKeys());
        return new StopAsyncResult();
    }

    @Override
    public void undo(StopAsyncAction action, StopAsyncResult result, ExecutionContext context) throws ActionException {

    }
}
