package com.aplana.sbrf.taxaccounting.web.module.lock.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.DeleteLockAction;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.DeleteLockResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Удаление блокировок
 * @author dloshkarev
 */
@Service
public class DeleteLockHandler extends AbstractActionHandler<DeleteLockAction, DeleteLockResult> {

    @Autowired
    LockDataService lockService;

    @Autowired
    SecurityService securityService;

    public DeleteLockHandler() {
        super(DeleteLockAction.class);
    }

    @Override
    public DeleteLockResult execute(DeleteLockAction action, ExecutionContext context) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        lockService.interruptAllTasks(action.getKeys(), userInfo.getUser().getId(), "Удалена блокировка задачи (форма \"Список блокировок\")");
        return new DeleteLockResult();
    }

    @Override
    public void undo(DeleteLockAction action, DeleteLockResult result, ExecutionContext context) throws ActionException {

    }
}
