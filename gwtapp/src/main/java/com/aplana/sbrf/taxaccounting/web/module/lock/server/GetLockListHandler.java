package com.aplana.sbrf.taxaccounting.web.module.lock.server;

import com.aplana.sbrf.taxaccounting.common.model.UserInfo;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskState;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Получение списка блокировок
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP')")
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

        PagingResult<LockData> records = lockService.getLocks(action.getFilter(), action.getPagingParams());
        for (LockData lockData : records) {
            TAUser user = userService.getUser(lockData.getUserId());
            LockDataItem lock = new LockDataItem();
            lock.setKey(lockData.getKey());
            lock.setDescription(lockData.getDescription());
            lock.setUser(TAUser.SYSTEM_USER_ID != user.getId() ? user.getName() + " (" + user.getLogin() + ")" : user.getName());
            lock.setUserId(user.getId());
            lock.setDateBefore(df.format(lockData.getDateBefore()));
            lock.setDateLock(df.format(lockData.getDateLock()));
            lock.setState(lockData.getState());
            lock.setStateDate(lockData.getStateDate() != null ? df.format(lockData.getStateDate()) : null);
            lock.setQueue(lockData.getQueue());
            lock.setQueuePosition(lock.getQueue() != null ? lockData.getQueuePosition() : -1);
            locks.add(lock);
        }

        TAUser currentUser = securityService.currentUserInfo().getUser();
        result.setLocks(locks);
        result.setTotalCountOfRecords(records.getTotalCount());
        result.setCurrentUserId(currentUser.getId());
        result.setHasRoleAdmin(currentUser.hasRole(TARole.ROLE_ADMIN));
        return result;
    }

    @Override
    public void undo(GetLockListAction getTaskListAction, GetLockListResult getTaskListResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
