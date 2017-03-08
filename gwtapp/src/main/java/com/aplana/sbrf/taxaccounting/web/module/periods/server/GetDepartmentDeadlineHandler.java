package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetDepartmentDeadlineAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetDepartmentDeadlineResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class GetDepartmentDeadlineHandler extends AbstractActionHandler<GetDepartmentDeadlineAction, GetDepartmentDeadlineResult> {

    @Autowired
    private NotificationService notificationService;

    public GetDepartmentDeadlineHandler() {
        super(GetDepartmentDeadlineAction.class);
    }

    @Override
    public GetDepartmentDeadlineResult execute(GetDepartmentDeadlineAction action, ExecutionContext executionContext) throws ActionException {
        GetDepartmentDeadlineResult result = new GetDepartmentDeadlineResult();
        Notification notification = notificationService.get(action.getReportPeriodId(), action.getSenderDepartmentId(), action.getReceiverDepartmentId());
        result.setDeadline(notification != null ? notification.getDeadline() : null);
        return result;
    }

    @Override
    public void undo(GetDepartmentDeadlineAction getDepartmentDeadlineAction, GetDepartmentDeadlineResult getDepartmentDeadlineResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
