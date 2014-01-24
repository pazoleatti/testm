package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.UpdateDepartmentDeadlineAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.UpdateDepartmentDeadlineResult;
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

@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class UpdateDepartmentDeadlineHandler extends AbstractActionHandler<UpdateDepartmentDeadlineAction, UpdateDepartmentDeadlineResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DepartmentService departmentService;

    public UpdateDepartmentDeadlineHandler() {
        super(UpdateDepartmentDeadlineAction.class);
    }

    @Override
    public UpdateDepartmentDeadlineResult execute(UpdateDepartmentDeadlineAction action, ExecutionContext executionContext) throws ActionException {
        UpdateDepartmentDeadlineResult result = new UpdateDepartmentDeadlineResult();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        TAUserInfo userInfo = securityService.currentUserInfo();

        String text = "%s назначил новый срок сдачи отчетности подразделению %s для %s в периоде %s %s года: %s";
        List<Notification> notifications = new ArrayList<Notification>();
        for (DepartmentPair pair : action.getDepartments()) {
            Department receiver =
		            pair.getParentDepartmentId() == null
		            ? null
		            : departmentService.getDepartment(pair.getParentDepartmentId());

            Notification notification = new Notification();
            notification.setCreateDate(new Date());
            notification.setDeadline(action.getDeadline());
            notification.setReportPeriodId(action.getReportPeriodId());
            notification.setSenderDepartmentId(pair.getDepartmentId());
            notification.setReceiverDepartmentId(pair.getParentDepartmentId());
            notification.setText(String.format(text,
                    userInfo.getUser().getName(), receiver == null ? "" : receiver.getName(), action.getTaxType().getName(),
                    action.getReportPeriodName(), action.getCurrentYear(), df.format(action.getDeadline())));

            notifications.add(notification);
        }

        notificationService.saveList(notifications);
        return result;
    }

    @Override
    public void undo(UpdateDepartmentDeadlineAction updateDepartmentDeadlineAction,
                     UpdateDepartmentDeadlineResult updateDepartmentDeadlineResult,
                     ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
