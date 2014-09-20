package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
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

import javax.management.NotificationFilter;
import java.util.ArrayList;
import java.util.List;

@Component
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS', 'ROLE_OPER')")
public class GetNotificationCountHandler extends AbstractActionHandler<GetNotificationCountAction, GetNotificationCountResult> {

	public GetNotificationCountHandler() {
		super(GetNotificationCountAction.class);
	}

	@Autowired
	NotificationService notificationService;
	@Autowired
	SecurityService securityService;

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
        filter.setReceiverDepartmentId(user.getDepartmentId());
        filter.setUserRoleIds(userRoles);
		result.setNotificationCount(notificationService.getCountByFilter(filter));
		return result;
	}

	@Override
	public void undo(GetNotificationCountAction getNotificationCountAction, GetNotificationCountResult getNotificationCountResult, ExecutionContext executionContext) throws ActionException {

	}
}
