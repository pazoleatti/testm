package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerTaskResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

/**
 * @author lhaziev
 *
 */
@Service
public class TimerTaskHandler extends AbstractActionHandler<TimerTaskAction, TimerTaskResult> {

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TAUserService taUserService;

    public TimerTaskHandler() {
        super(TimerTaskAction.class);
    }

    @Override
    public TimerTaskResult execute(TimerTaskAction action, ExecutionContext executionContext) throws ActionException {
        TimerTaskResult result = new TimerTaskResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
        if (lockType != null) {
            result.setTaskType(lockType.getFirst());
            result.setLockedByUser(taUserService.getUser(lockType.getSecond().getUserId()).getName());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
            result.setLockDate(formatter.format(lockType.getSecond().getDateLock()));
        }
        LockData lockInformation = formDataService.getObjectLock(action.getFormDataId(), userInfo);
        if (lockInformation != null && lockInformation.getUserId() == userInfo.getUser().getId()) {
            if (ReportType.EDIT_FD.equals(lockType.getFirst())) {
                // есто только блокировка режима редактирования
                result.setFormMode(TimerTaskResult.FormMode.EDIT);
            } else {
                result.setFormMode(TimerTaskResult.FormMode.LOCKED_EDIT);
            }
        } else {
            if (lockType == null) {
                result.setFormMode(TimerTaskResult.FormMode.EDIT);
            } else {
                // есто блокировка
                result.setFormMode(TimerTaskResult.FormMode.LOCKED);
            }
        }
        return result;
    }

    @Override
    public void undo(TimerTaskAction searchAction, TimerTaskResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
