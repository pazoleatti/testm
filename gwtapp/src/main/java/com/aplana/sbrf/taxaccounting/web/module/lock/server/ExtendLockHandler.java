package com.aplana.sbrf.taxaccounting.web.module.lock.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.DeleteLockAction;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.DeleteLockResult;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.ExtendLockAction;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.ExtendLockResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Продление блокировок
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP')")
public class ExtendLockHandler extends AbstractActionHandler<ExtendLockAction, ExtendLockResult> {

    @Autowired
    LockDataService lockService;

    public ExtendLockHandler() {
        super(ExtendLockAction.class);
    }

    @Override
    public ExtendLockResult execute(ExtendLockAction action, ExecutionContext context) throws ActionException {
        lockService.extendAll(action.getKeys(), 1);
        return new ExtendLockResult();
    }

    @Override
    public void undo(ExtendLockAction action, ExtendLockResult result, ExecutionContext context) throws ActionException {

    }
}
