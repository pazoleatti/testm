package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerTaskResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lhaziev
 *
 */
@Service
public class TimerTaskHandler extends AbstractActionHandler<TimerTaskAction, TimerTaskResult> {

    @Autowired
    private FormDataService formDataService;


    public TimerTaskHandler() {
        super(TimerTaskAction.class);
    }

    @Override
    public TimerTaskResult execute(TimerTaskAction action, ExecutionContext executionContext) throws ActionException {
        TimerTaskResult result = new TimerTaskResult();
        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
        if (lockType != null) result.setTaskType(lockType.getFirst());
        return result;
    }

    @Override
    public void undo(TimerTaskAction searchAction, TimerTaskResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
