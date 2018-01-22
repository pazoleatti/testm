package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationCountAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationCountResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PreAuthorize("isAuthenticated()")
public class GetNotificationCountHandler extends AbstractActionHandler<GetNotificationCountAction, GetNotificationCountResult> {

	public GetNotificationCountHandler() {
		super(GetNotificationCountAction.class);
	}

	@Autowired
	NotificationService notificationService;
	@Autowired
	SecurityService securityService;
    @Autowired
    DepartmentService departmentService;

	@Override
	public GetNotificationCountResult execute(GetNotificationCountAction getNotificationCountAction, ExecutionContext executionContext) throws ActionException {
		GetNotificationCountResult result = new GetNotificationCountResult();
        TAUser user = securityService.currentUserInfo().getUser();
        List<Integer> userRoles = new ArrayList<Integer>();
        for (TARole role : user.getRoles()) {
            userRoles.add(role.getId());
        }
        NotificationsFilterData filter = new NotificationsFilterData();
        filter.setUserId(user.getId());
        //filter.setReceiverDepartmentIds(departmentService.getTaxFormDepartments(user, asList(TaxType.values()), null, null));
        filter.setUserRoleIds(userRoles);
        filter.setRead(false);
		result.setNotificationCount(notificationService.fetchCountByFilter(filter));
        result.setLastNotificationDate(notificationService.fetchLastNotificationDate());

        if (user.getRoles().hashCode() != getNotificationCountAction.getRolesHashCode()) {
            // изменился набор ролей
            result.setEditedRoles(true);
            securityService.updateUserRoles();
        }
        return result;
	}

	@Override
	public void undo(GetNotificationCountAction getNotificationCountAction, GetNotificationCountResult getNotificationCountResult, ExecutionContext executionContext) throws ActionException {

	}
}
