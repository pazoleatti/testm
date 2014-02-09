package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

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

@Component
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
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
		result.setNotificationCount(notificationService.getCount(securityService.currentUserInfo().getUser().getDepartmentId()));
		return result;
	}

	@Override
	public void undo(GetNotificationCountAction getNotificationCountAction, GetNotificationCountResult getNotificationCountResult, ExecutionContext executionContext) throws ActionException {

	}
}
