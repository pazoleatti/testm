package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckReceiversAcceptedPreparedAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckReceiversAcceptedPreparedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class CheckReceiversAcceptedPreparedHandler extends AbstractActionHandler<CheckReceiversAcceptedPreparedAction, CheckReceiversAcceptedPreparedResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LogEntryService logEntryService;

    public CheckReceiversAcceptedPreparedHandler() {
        super(CheckReceiversAcceptedPreparedAction.class);
    }

    @Override
    public CheckReceiversAcceptedPreparedResult execute(CheckReceiversAcceptedPreparedAction action, ExecutionContext context) throws ActionException {
        final CheckReceiversAcceptedPreparedResult result = new CheckReceiversAcceptedPreparedResult();

        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        List<Long> resultIdList = declarationDataService.getReceiversAcceptedPrepared(action.getDeclarationDataId(), logger, userInfo);
        result.setReceiversAcceptedPreparedIdList(resultIdList);
        if (!resultIdList.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder("Ошибка изменения состояния формы № " + action.getDeclarationDataId() + ". Данная форма не может быть возвращена в состояние 'Создана', так как используются приемники с состоянием 'Принята/Подготовлена', номера форм: ");
            for (Long id : resultIdList) {
                messageBuilder.append(id)
                        .append(", ");
            }
            messageBuilder.delete(messageBuilder.length() - 2, messageBuilder.length());
            logger.error(messageBuilder.toString());
            sendNotifications(messageBuilder.toString(), logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
        }
        return result;
    }

    @Override
    public void undo(CheckReceiversAcceptedPreparedAction action, CheckReceiversAcceptedPreparedResult result, ExecutionContext context) throws ActionException {

    }

    private void sendNotifications(String msg, String uuid, Integer userId, NotificationType notificationType, String reportId) {
        if (msg != null && !msg.isEmpty()) {
            List<Notification> notifications = new ArrayList<Notification>();
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setCreateDate(new LocalDateTime());
            notification.setText(msg);
            notification.setLogId(uuid);
            notification.setReportId(reportId);
            notification.setNotificationType(notificationType);
            notifications.add(notification);
            notificationService.saveList(notifications);
        }
    }
}
