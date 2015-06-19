package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.LockInfo;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.TimerTaskResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author lhaziev
 *
 */
@Service
public class TimerTaskHandler extends AbstractActionHandler<TimerTaskAction, TimerTaskResult> {

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private FormDataAccessService formDataAccessService;

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
        LockInfo lockInfo = new LockInfo();
        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
        if (lockType != null) {
            result.setTaskType(lockType.getFirst());
            lockInfo.setLockedByUser(taUserService.getUser(lockType.getSecond().getUserId()).getName());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
            lockInfo.setLockDate(formatter.format(lockType.getSecond().getDateLock()));
            lockInfo.setTitle(String.format("Запущена операция \"%s\"", formDataService.getTaskName(lockType.getFirst(), action.getFormDataId(), userInfo)));
        }
        LockData lockInformation = formDataService.getObjectLock(action.getFormDataId(), userInfo);
        if (lockInformation != null) {
            lockInfo.setEditMode(true);
            if (lockInformation.getUserId() == userInfo.getUser().getId()) {
                lockInfo.setLockedMe(true);
            } else {
                lockInfo.setLockedMe(false);
            }
        } else {
            lockInfo.setEditMode(false);
            if (lockType != null && lockType.getSecond().getUserId() == userInfo.getUser().getId()) {
                lockInfo.setLockedMe(true);
            } else {
                lockInfo.setLockedMe(false);
            }
        }
        result.setLockInfo(lockInfo);
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
            } else if (lockType.getSecond().getUserId() == userInfo.getUser().getId()) {
                result.setFormMode(TimerTaskResult.FormMode.LOCKED_READ);
            } else {
                result.setFormMode(TimerTaskResult.FormMode.LOCKED);
            }
        }
        return result;
    }

    @Override
    public void undo(TimerTaskAction searchAction, TimerTaskResult searchResult, ExecutionContext executionContext) throws ActionException {

    }

}
