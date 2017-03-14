package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.UpdateNotificationStatusAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.UpdateNotificationStatusResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@Component
@PreAuthorize("isAuthenticated()")
public class UpdateNotificationStatusHandler extends AbstractActionHandler<UpdateNotificationStatusAction, UpdateNotificationStatusResult> {

    public UpdateNotificationStatusHandler() {
        super(UpdateNotificationStatusAction.class);
    }

    @Autowired
    NotificationService notificationService;
    @Autowired
    SecurityService securityService;
    @Autowired
    DepartmentService departmentService;

    @Override
    public UpdateNotificationStatusResult execute(UpdateNotificationStatusAction action, ExecutionContext context) throws ActionException {
        TAUser user = securityService.currentUserInfo().getUser();
        List<Integer> userRoles = new ArrayList<Integer>();
        for (TARole role : user.getRoles()) {
            userRoles.add(role.getId());
        }
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        //filter.setReceiverDepartmentIds(departmentService.getTaxFormDepartments(user, asList(TaxType.values()), null, null));
        filter.setUserRoleIds(userRoles);
        notificationService.updateUserNotificationsStatus(filter);
        return new UpdateNotificationStatusResult();
    }

    @Override
    public void undo(UpdateNotificationStatusAction action, UpdateNotificationStatusResult result, ExecutionContext context) throws ActionException {
        //Do nothing
    }
}
