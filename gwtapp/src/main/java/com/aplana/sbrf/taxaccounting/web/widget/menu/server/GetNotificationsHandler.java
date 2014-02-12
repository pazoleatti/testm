package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationsAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationsResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.NotificationTableRow;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS', 'ROLE_OPER')")
public class GetNotificationsHandler extends AbstractActionHandler<GetNotificationsAction, GetNotificationsResult> {

	public GetNotificationsHandler() {
		super(GetNotificationsAction.class);
	}

	@Autowired
	NotificationService notificationService;
	@Autowired
	SecurityService securityService;

	@Override
	public GetNotificationsResult execute(GetNotificationsAction action, ExecutionContext executionContext) throws ActionException {
		NotificationsFilterData filter = new NotificationsFilterData();
		filter.setCountOfRecords(action.getLength());
		filter.setStartIndex(action.getStart());
		filter.setReceiverDepartmentId(securityService.currentUserInfo().getUser().getDepartmentId());
		List<NotificationTableRow> rows = new ArrayList<NotificationTableRow>();
		PagingResult<Notification> result = notificationService.getByFilter(filter);
		for (Notification notification : result) {
			NotificationTableRow row = new NotificationTableRow();
			row.setDate(notification.getCreateDate());
			row.setMsg(notification.getText());
			rows.add(row);
		}
		PagingResult<NotificationTableRow> resultRows = new PagingResult<NotificationTableRow>(rows, result.getTotalCount());
		GetNotificationsResult actionResult = new GetNotificationsResult();
		actionResult.setRows(resultRows);

		return actionResult;
	}

	@Override
	public void undo(GetNotificationsAction getNotificationsAction, GetNotificationsResult getNotificationsResult, ExecutionContext executionContext) throws ActionException {
	}
}
