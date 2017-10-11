package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.TimerAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.TimerResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
@PreAuthorize("isAuthenticated()")
public class TimerHandler extends AbstractActionHandler<TimerAction, TimerResult> {
    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private TAUserService taUserService;

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    public TimerHandler() {
        super(TimerAction.class);
    }

    @Override
    public TimerResult execute(TimerAction action, ExecutionContext executionContext) throws ActionException {
        TimerResult result = new TimerResult();
        RefBook refBook = refBookFactory.get(action.getRefBookId());

        LockData lockData = lockDataService.getLock(refBookFactory.generateTaskKey(refBook.getId()));
        if (lockData != null) {
            result.setLock(true);
            result.setText(refBookFactory.getRefBookLockDescription(lockData, refBook.getId()));
            result.setLockId(lockData.getKey() + "_" + lockData.getDateLock().getTime());
        } else {
            result.setLock(false);
        }
        return result;
    }

    @Override
    public void undo(TimerAction action, TimerResult result, ExecutionContext executionContext) throws ActionException {

    }
}
