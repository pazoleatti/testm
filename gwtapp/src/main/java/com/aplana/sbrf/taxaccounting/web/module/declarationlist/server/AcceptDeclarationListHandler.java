package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.AcceptDeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.AcceptDeclarationListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class AcceptDeclarationListHandler extends AbstractActionHandler<AcceptDeclarationListAction, AcceptDeclarationListResult> {

    private static final Log LOG = LogFactory.getLog(AcceptDeclarationListHandler.class);

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

    public AcceptDeclarationListHandler() {
        super(AcceptDeclarationListAction.class);
    }

    @Override
    public AcceptDeclarationListResult execute(final AcceptDeclarationListAction action, ExecutionContext context) throws ActionException {
        final DeclarationDataReportType ddToAcceptedReportType = DeclarationDataReportType.ACCEPT_DEC;
        final DeclarationDataReportType toCreatedReportType = DeclarationDataReportType.TO_CREATE_DEC;
        final AcceptDeclarationListResult result = new AcceptDeclarationListResult();
        final Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        final String acceptTaskName = declarationDataService.getTaskName(ddToAcceptedReportType, action.getTaxType());
        final String toCreateTaskName = declarationDataService.getTaskName(toCreatedReportType, action.getTaxType());
        String uuid = "";
        for (Long id: action.getDeclarationIds()) {
            if (declarationDataService.existDeclarationData(id)) {
                final Long declarationId = id;
                if (action.isAccepted()) {
                    final String prefix = String.format("Постановка операции \"%s\" для формы № %d в очередь на исполнение: ", acceptTaskName, declarationId);
                    try {
                        String uuidXml = reportService.getDec(userInfo, declarationId, DeclarationDataReportType.XML_DEC);
                        if (uuidXml != null) {
                            DeclarationData declarationData = declarationDataService.get(declarationId, userInfo);
                            if (!declarationData.getState().equals(State.ACCEPTED)) {
                                String keyTask = declarationDataService.generateAsyncTaskKey(declarationId, ddToAcceptedReportType);
                                Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, declarationDataService.getTaskName(ddToAcceptedReportType, action.getTaxType()), userInfo, false, logger);
                                if (restartStatus != null && restartStatus.getFirst()) {
                                    logger.warn(prefix + "Данная операция уже запущена");
                                } else if (restartStatus != null && !restartStatus.getFirst()) {
                                    // задача уже была создана, добавляем пользователя в получатели
                                } else {
                                    Map<String, Object> params = new HashMap<String, Object>();
                                    params.put("declarationDataId", declarationId);
                                    asyncTaskManagerService.createTask(keyTask, ddToAcceptedReportType.getReportType(), params, false, userInfo, logger, new AsyncTaskHandler() {
                                        @Override
                                        public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                                            return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                                    declarationDataService.getDeclarationFullName(declarationId, ddToAcceptedReportType),
                                                    LockData.State.IN_QUEUE.getText());
                                        }

                                        @Override
                                        public void executePostCheck() {
                                            logger.error(prefix + "Найдена запущенная задача, которая блокирует выполнение операции.");
                                        }

                                        @Override
                                        public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                                            return declarationDataService.checkExistTask(declarationId, reportType, logger);
                                        }

                                        @Override
                                        public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                                            declarationDataService.interruptTask(declarationId, userInfo, reportType, TaskInterruptCause.DECLARATION_ACCEPT);
                                        }

                                        @Override
                                        public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                                            return declarationDataService.getTaskName(ddToAcceptedReportType, action.getTaxType());
                                        }
                                    });
                                }
                            } else {
                                logger.error(prefix + "Налоговая форма уже находиться в статусе \"%s\".", State.ACCEPTED.getTitle());
                            }
                        } else {
                            logger.error(prefix + "Экземпляр налоговой формы не заполнен данными.");
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                        logger.error(prefix + e.getMessage());
                    }
                } else {
                    //logger.info("Постановка операции \"%s\" в очередь на исполнение для объекта: %s", toCreateTaskName, declarationDataService.getDeclarationFullName(declarationId, null));
                    String declarationFullName = declarationDataService.getDeclarationFullName(declarationId, DeclarationDataReportType.TO_CREATE_DEC);

                    // Блокировка формы
                    LockData lockData = lockDataService.lock(declarationDataService.generateAsyncTaskKey(declarationId, DeclarationDataReportType.TO_CREATE_DEC),
                            userInfo.getUser().getId(), declarationFullName);

                    if (lockData != null) {
                        DeclarationData declaration = declarationDataService.get(declarationId, userInfo);
                        Department department = departmentService.getDepartment(declaration.getDepartmentId());
                        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declaration.getDeclarationTemplateId());
                        logger.error(LOCK_MSG, declarationTemplate.getType().getName(), department.getName());
                        continue;
                    }
                    logger.info("Операция \"Возврат в Создана\" для налоговой формы № %d поставлена в очередь на исполнение", declarationId);
                    String message = "";
                    try {
                        List<Long> receiversIdList = declarationDataService.getReceiversAcceptedPrepared(declarationId, logger, userInfo);
                        if (!receiversIdList.isEmpty()) {
                            message = getCheckReceiversErrorMessage(receiversIdList);
                            logger.error(message);
                            sendNotifications(message, logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                            logger.clear();
                            continue;
                        }
                        declarationDataService.cancel(logger, declarationId, action.getReasonForReturn(), securityService.currentUserInfo());
                        message = new Formatter().format("Налоговая форма № %d успешно переведена в статус \"%s\".", declarationId, State.CREATED.getTitle()).toString();
                        logger.info(message);
                        sendNotifications("Выполнена операция \"Возврат в Создана\"", logEntryService.save(logger.getEntries()), userInfo.getUser().getId(), NotificationType.DEFAULT, null);
                        logger.clear();
                    } catch (Exception e) {
                        logger.error(e);
                    } finally {
                        lockDataService.unlock(declarationDataService.generateAsyncTaskKey(declarationId, DeclarationDataReportType.TO_CREATE_DEC), userInfo.getUser().getId());
                    }
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, id);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(AcceptDeclarationListAction action, AcceptDeclarationListResult result, ExecutionContext context) throws ActionException {
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
