package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.DeleteNotificationAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.DeleteNotificationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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
        notificationService.deleteAll(action.getNotificationIds());
        return result;
    }

    @Override
    public void undo(DeleteNotificationAction action, DeleteNotificationResult result, ExecutionContext context) throws ActionException {
        //Do nothing
    }
}
