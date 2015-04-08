package com.aplana.sbrf.taxaccounting.web.module.lock.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
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
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class GetLockListHandler extends AbstractActionHandler<GetLockListAction, GetLockListResult> {

    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";

    @Autowired
    LockDataService lockService;

    @Autowired
    TAUserService userService;

    public GetLockListHandler() {
        super(GetLockListAction.class);
    }

    @Override
    public GetLockListResult execute(GetLockListAction action, ExecutionContext executionContext) throws ActionException {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        GetLockListResult result = new GetLockListResult();
        List<LockDataItem> locks = new ArrayList<LockDataItem>();

        System.out.println("sleep started!");
        try {
            Thread.sleep(3600000 * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PagingResult<LockData> records = lockService.getLocks(action.getFilter(), action.getStartIndex(), action.getCountOfRecords(),
                action.getSearchOrdering(), action.isAscSorting());
        for (LockData lockData : records) {
            LockDataItem lock = new LockDataItem();
            lock.setKey(lockData.getKey());
            lock.setUserLogin(userService.getUser(lockData.getUserId()).getLogin());
            lock.setDateBefore(df.format(lockData.getDateBefore()));
            lock.setDateLock(df.format(lockData.getDateLock()));
            locks.add(lock);
        }
        System.out.println("sleep finished!");

        result.setLocks(locks);
        result.setTotalCountOfRecords(records.getTotalCount());
        return result;
    }

    @Override
    public void undo(GetLockListAction getTaskListAction, GetLockListResult getTaskListResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
