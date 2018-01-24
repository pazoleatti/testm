package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
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

import java.util.*;

@Component
@PreAuthorize("isAuthenticated()")
public class GetNotificationsHandler extends AbstractActionHandler<GetNotificationsAction, GetNotificationsResult> {

	public GetNotificationsHandler() {
		super(GetNotificationsAction.class);
	}

	@Autowired
	NotificationService notificationService;
	@Autowired
	SecurityService securityService;
    @Autowired
    DepartmentService departmentService;

	@Override
	public GetNotificationsResult execute(GetNotificationsAction action, ExecutionContext executionContext) throws ActionException {
        TAUser user = securityService.currentUserInfo().getUser();
        List<Integer> userRoles = new ArrayList<Integer>();
        for (TARole role : user.getRoles()) {
            userRoles.add(role.getId());
        }
        NotificationsFilterData filter = action.getFilter();
        filter.setUserId(user.getId());
		Set<Integer> receiverDepartmentIds = new HashSet<Integer>();
		receiverDepartmentIds.addAll(departmentService.getNDFLDeclarationDepartments(user));
        filter.setReceiverDepartmentIds(new ArrayList<Integer>(receiverDepartmentIds));
        filter.setUserRoleIds(userRoles);

		List<NotificationTableRow> rows = new ArrayList<NotificationTableRow>();
		PagingResult<Notification> result = notificationService.fetchByFilter(filter);
		for (Notification notification : result) {
			NotificationTableRow row = new NotificationTableRow();
            row.setId(notification.getId());
			row.setDate(notification.getCreateDate());
			row.setMsg(notification.getText());
            row.setLogId(notification.getLogId());
            row.setCanDelete(canDelete(notification, user));
            row.setReportId(notification.getReportId());
            row.setNotificationType(notification.getNotificationType());
			rows.add(row);
		}
		PagingResult<NotificationTableRow> resultRows = new PagingResult<NotificationTableRow>(rows, result.getTotalCount());
		GetNotificationsResult actionResult = new GetNotificationsResult();
		actionResult.setRows(resultRows);

		return actionResult;
	}

    /**
     * Получить признак возможности удаления оповещения.
     *
     * @param notification оповещение
     * @param user пользователь
     */
    private Boolean canDelete(Notification notification, TAUser user) {
        // пользователь может удалить оповещение:
        // если заполнен ID пользователя-получателя (для любой роли) или ID роли получателя (для любой роли)
        // или ID подразделения-получателя (для роли Контролёр УНП или Контролёр НС)
        //ID пользователя и роли проверяются на != null, а не на > 0 (как было раньше), потому что в JDBC, если поле
        //в таблице содержит значение NULL, то при чтении возвращается 0, в querydsl возвращается null
        return notification.getUserId() != null || notification.getRoleId() != null ||
                (notification.getReceiverDepartmentId() != null && (user.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP) ||
                                (user.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS) && user.getDepartmentId() == notification.getReceiverDepartmentId())));

    }

    @Override
	public void undo(GetNotificationsAction getNotificationsAction, GetNotificationsResult getNotificationsResult, ExecutionContext executionContext) throws ActionException {
	}
}
