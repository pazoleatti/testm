package com.aplana.sbrf.taxaccounting.web.module.lock.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.GetLockListAction;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.GetLockListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Получение списка блокировок
 * @author dloshkarev
 */
@Service
public class GetLockListHandler extends AbstractActionHandler<GetLockListAction, GetLockListResult> {

    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

    @Autowired
    LockDataService lockService;

    @Autowired
    TAUserService userService;

    @Autowired
    SecurityService securityService;

    public GetLockListHandler() {
        super(GetLockListAction.class);
    }

    @Override
    public GetLockListResult execute(GetLockListAction action, ExecutionContext executionContext) throws ActionException {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        GetLockListResult result = new GetLockListResult();
        List<LockDataItem> locks = new ArrayList<LockDataItem>();

        PagingResult<LockData> records = lockService.getLocks(action.getFilter(), action.getQueues(), action.getPagingParams());
        for (LockData lockData : records) {
            TAUser user = userService.getUser(lockData.getUserId());
            LockDataItem lock = new LockDataItem();
            lock.setKey(lockData.getKey());
            lock.setDescription(lockData.getDescription());
            lock.setUser(TAUser.SYSTEM_USER_ID != user.getId() ? user.getName() + " (" + user.getLogin() + ")" : user.getName());
            lock.setUserId(user.getId());
            lock.setDateLock(df.format(lockData.getDateLock()));
            lock.setStateDate(lockData.getStateDate() != null ? df.format(lockData.getStateDate()) : null);
            if (lockData.getQueue() == LockData.LockQueues.NONE) {
                lock.setQueue("");
                lock.setQueuePosition(-1);
            } else {
                lock.setQueue(lockData.getQueue().getText());
                lock.setQueuePosition(lock.getQueue() != null ? lockData.getQueuePosition() : -1);
            }
            lock.setState(lockData.getState());
            if (lock.getState() != null && lock.getQueuePosition() == 1 && lock.getState().equals(LockData.State.IN_QUEUE.getText())) {
                lock.setState(LockData.State.LOCKED.getText());
            }
            lock.setServerNode(lockData.getServerNode());
            locks.add(lock);
        }

        TAUser currentUser = securityService.currentUserInfo().getUser();
        result.setLocks(locks);
        result.setTotalCountOfRecords(records.getTotalCount());
        result.setCurrentUserId(currentUser.getId());
        result.setHasRoleAdmin(currentUser.hasRole(TARole.N_ROLE_ADMIN));
        return result;
    }

    @Override
    public void undo(GetLockListAction getTaskListAction, GetLockListResult getTaskListResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
