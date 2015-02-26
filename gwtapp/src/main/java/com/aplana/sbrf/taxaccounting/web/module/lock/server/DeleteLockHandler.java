package com.aplana.sbrf.taxaccounting.web.module.lock.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.DeleteLockAction;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.DeleteLockResult;
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
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class DeleteLockHandler extends AbstractActionHandler<DeleteLockAction, DeleteLockResult> {

    @Autowired
    LockDataService lockService;

    public DeleteLockHandler() {
        super(DeleteLockAction.class);
    }

    @Override
    public DeleteLockResult execute(DeleteLockAction action, ExecutionContext context) throws ActionException {
        lockService.unlockAll(action.getKeys());
        return new DeleteLockResult();
    }

    @Override
    public void undo(DeleteLockAction action, DeleteLockResult result, ExecutionContext context) throws ActionException {

    }
}
