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

import javax.management.NotificationFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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
		result.setNotificationCount(notificationService.getCountByFilter(filter));
		return result;
	}

	@Override
	public void undo(GetNotificationCountAction getNotificationCountAction, GetNotificationCountResult getNotificationCountResult, ExecutionContext executionContext) throws ActionException {

	}
}
