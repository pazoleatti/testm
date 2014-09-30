package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.DeleteNotificationAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.DeleteNotificationResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationCountAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS', 'ROLE_OPER')")
public class DeleteNotificationHandler extends AbstractActionHandler<DeleteNotificationAction, DeleteNotificationResult> {

    @Autowired
    NotificationService notificationService;
    @Autowired
    SecurityService securityService;

    public DeleteNotificationHandler() {
        super(DeleteNotificationAction.class);
    }

    @Override
    public DeleteNotificationResult execute(DeleteNotificationAction action, ExecutionContext context) throws ActionException {
        DeleteNotificationResult result = new DeleteNotificationResult();
        if (action.isDeleteWithoutCheck()) {
            notificationService.deleteAll(action.getNotificationIds());
        } else {
            result.setAllowedNotifications(notificationService.getAllowedNotifications(securityService.currentUserInfo().getUser(), action.getNotificationIds()));
        }
        return result;
    }

    @Override
    public void undo(DeleteNotificationAction action, DeleteNotificationResult result, ExecutionContext context) throws ActionException {
        //Do nothing
    }
}
