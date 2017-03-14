package com.aplana.sbrf.taxaccounting.web.widget.menu.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.CheckReportNotificationAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.CheckReportNotificationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("isAuthenticated()")
public class CheckReportNotificationHandler extends AbstractActionHandler<CheckReportNotificationAction, CheckReportNotificationResult> {

    public CheckReportNotificationHandler() {
        super(CheckReportNotificationAction.class);
    }

    @Autowired
    NotificationService notificationService;
    @Autowired
    SecurityService securityService;
    @Autowired
    DepartmentService departmentService;

    @Override
    public CheckReportNotificationResult execute(CheckReportNotificationAction action, ExecutionContext context) throws ActionException {
        TAUser user = securityService.currentUserInfo().getUser();
        Notification notification = notificationService.get(action.getId());
        CheckReportNotificationResult result = new CheckReportNotificationResult();
        result.setNotificationType(notification.getNotificationType());
        if (notification.getUserId() == user.getId()) {
            switch (notification.getNotificationType()) {
                case REF_BOOK_REPORT:
                    if (notification.getReportId() != null) {
                        result.setExist(true);
                        result.setReportId(notification.getReportId());
                    } else {
                        result.setExist(false);
                        result.setMsg("Отчет не найден. Возможно он был удален, сформируйте заново.");
                    }
                    break;
                case DEFAULT:
                    break;
            }
        }
        return result;
    }

    @Override
    public void undo(CheckReportNotificationAction action, CheckReportNotificationResult result, ExecutionContext context) throws ActionException {
        //Do nothing
    }
}
