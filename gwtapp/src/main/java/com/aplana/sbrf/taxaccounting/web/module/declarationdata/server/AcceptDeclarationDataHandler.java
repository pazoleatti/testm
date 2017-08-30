package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class AcceptDeclarationDataHandler extends AbstractActionHandler<AcceptDeclarationDataAction, AcceptDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    private static final String LOCK_MSG = "Форма \"%s\" из \"%s\" заблокирована";

    public AcceptDeclarationDataHandler() {
        super(AcceptDeclarationDataAction.class);
    }

    @Override
    public AcceptDeclarationDataResult execute(final AcceptDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddReportType = DeclarationDataReportType.ACCEPT_DEC;
        final AcceptDeclarationDataResult result = new AcceptDeclarationDataResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationId());
            return result;
        }
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        if (action.isAccepted()) {
            String uuidXml = reportService.getDec(userInfo, action.getDeclarationId(), DeclarationDataReportType.XML_DEC);
            if (uuidXml != null) {
                DeclarationData declarationData = declarationDataService.get(action.getDeclarationId(), userInfo);
                if (!declarationData.getState().equals(State.ACCEPTED)) {
                    String keyTask = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ddReportType);
                    Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationDataService.getTaskName(ddReportType, action.getTaxType()), userInfo, action.isForce(), logger);
                    if (restartStatus != null && restartStatus.getFirst()) {
                        result.setStatus(CreateAsyncTaskStatus.LOCKED);
                        result.setRestartMsg(restartStatus.getSecond());
                    } else if (restartStatus != null && !restartStatus.getFirst()) {
                        result.setStatus(CreateAsyncTaskStatus.CREATE);
                    } else {
                        result.setStatus(CreateAsyncTaskStatus.CREATE);
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("declarationDataId", action.getDeclarationId());
                        asyncTaskManagerService.createTask(keyTask, ddReportType.getReportType(), params, action.isCancelTask(), userInfo, logger, new AsyncTaskHandler() {
                            @Override
                            public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                                return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                        declarationDataService.getDeclarationFullName(action.getDeclarationId(), ddReportType),
                                        LockData.State.IN_QUEUE.getText());
                            }

                            @Override
                            public void executePostCheck() {
                                result.setStatus(CreateAsyncTaskStatus.EXIST_TASK);
                            }

                            @Override
                            public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                                return declarationDataService.checkExistTask(action.getDeclarationId(), reportType, logger);
                            }

                            @Override
                            public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                                declarationDataService.interruptTask(action.getDeclarationId(), userInfo, reportType, TaskInterruptCause.DECLARATION_ACCEPT);
                            }

                            @Override
                            public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                                return declarationDataService.getTaskName(ddReportType, action.getTaxType());
                            }
                        });
                    }
                } else {
                    result.setStatus(CreateAsyncTaskStatus.EXIST);
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
            }
        } else {
            final DeclarationDataReportType toCreatedReportType = DeclarationDataReportType.TO_CREATE_DEC;
            final String toCreateTaskName = declarationDataService.getTaskName(toCreatedReportType, action.getTaxType());
            String declarationFullName = declarationDataService.getDeclarationFullName(action.getDeclarationId(), null);
            //logger.info("Постановка операции \"%s\" в очередь на исполнение для объекта: %s", toCreateTaskName, declarationDataService.getDeclarationFullName(action.getDeclarationId(), null));
            // Блокировка формы
            LockData lockData = lockDataService.lock(declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), DeclarationDataReportType.TO_CREATE_DEC),
                    userInfo.getUser().getId(), declarationFullName);

            if (lockData != null) {
                DeclarationData declaration = declarationDataService.get(action.getDeclarationId(), userInfo);
                Department department = departmentService.getDepartment(declaration.getDepartmentId());
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
                logger.error(LOCK_MSG, declarationTemplate.getType().getName(), department.getName());
                String uuid = logEntryService.save(logger.getEntries());
                result.setUuid(uuid);
                sendNotifications(String.format(LOCK_MSG, declarationTemplate.getType().getName(), department.getName()), uuid, userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                return result;
            }
            logger.info("Операция \"Возврат в Создана\" для налоговой формы № %d поставлена в очередь на исполнение", action.getDeclarationId());
            try {
                List<Long> receiversIdList = declarationDataService.getReceiversAcceptedPrepared(action.getDeclarationId(), logger, userInfo);
                if (!receiversIdList.isEmpty()) {
                    DeclarationData declaration = declarationDataService.get(action.getDeclarationId(), userInfo);
                    /*Department department = departmentService.getDepartment(declaration.getDepartmentId());
                    DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());*/
                    String message = getCheckReceiversErrorMessage(receiversIdList);
                    logger.error(message);
                    String uuid = logEntryService.save(logger.getEntries());
                    result.setUuid(uuid);
                    sendNotifications(message, uuid, userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                    return result;
                }
                declarationDataService.cancel(logger, action.getDeclarationId(), action.getReasonForReturn(), securityService.currentUserInfo());
                String message = new Formatter().format("Налоговая форма № %d успешно переведена в статус \"%s\".", action.getDeclarationId(), State.CREATED.getTitle()).toString();
                logger.info(message);

            } catch (Exception e) {
                logger.error(e);
            } finally {
                lockDataService.unlock(declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), DeclarationDataReportType.TO_CREATE_DEC), userInfo.getUser().getId());
            }
            sendNotifications("Выполнена операция \"Возврат в Создана\"", logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
            result.setStatus(CreateAsyncTaskStatus.EXIST);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(AcceptDeclarationDataAction action, AcceptDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
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

    private String getCheckReceiversErrorMessage(List<Long> receivers) {
        StringBuilder sb = new StringBuilder("Отмена принятия текущей формы невозможна. Формы-приёмники ");
        for (Long receiver : receivers) {
            sb.append(receiver)
                    .append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(" имеют состояние, отличное от \"Создана\". Выполните \"Возврат в Создана\" для перечисленных форм и повторите операцию.");
        return sb.toString();
    }
}
