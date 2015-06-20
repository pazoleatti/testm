package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
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
    private AsyncManager asyncManager;

    @Autowired
    private TAUserService userService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm z");

    public AcceptDeclarationDataHandler() {
        super(AcceptDeclarationDataAction.class);
    }

    @Override
    public AcceptDeclarationDataResult execute(AcceptDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.ACCEPT_DEC;
        AcceptDeclarationDataResult result = new AcceptDeclarationDataResult();
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        if (action.isAccepted()) {
            Map<String, Object> params = new HashMap<String, Object>();
            String uuidXml = reportService.getDec(userInfo, action.getDeclarationId(), ReportType.XML_DEC);
            if (uuidXml != null) {
                DeclarationData declarationData = declarationDataService.get(action.getDeclarationId(), userInfo);
                if (!declarationData.isAccepted()) {
                    String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), reportType);
                    LockData lockDataReportTask = lockDataService.getLock(key);
                    if (lockDataReportTask != null && lockDataReportTask.getUserId() == userInfo.getUser().getId()) {
                        if (action.isForce()) {
                            // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                            lockDataService.interruptTask(lockDataReportTask, userInfo.getUser().getId(), false);
                        } else {
                            result.setStatus(CreateAsyncTaskStatus.LOCKED);
                            String restartMsg = (lockDataReportTask.getState().equals(LockData.State.IN_QUEUE.getText())) ?
                                    String.format(LockData.CANCEL_MSG, String.format(ReportType.ACCEPT_DEC.getDescription(), action.getTaxType().getDeclarationShortName())) :
                                    String.format(LockData.RESTART_MSG, String.format(ReportType.ACCEPT_DEC.getDescription(), action.getTaxType().getDeclarationShortName()));
                            result.setRestartMsg(restartMsg);
                            return result;
                        }
                    } else if (lockDataReportTask != null) {
                        try {
                            lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                            logger.info(String.format(LockData.LOCK_INFO_MSG,
                                    String.format(ReportType.ACCEPT_DEC.getDescription(), action.getTaxType().getDeclarationShortName()),
                                    sdf.format(lockDataReportTask.getDateLock()),
                                    userService.getUser(lockDataReportTask.getUserId()).getName()));
                        } catch (ServiceException e) {
                        }
                        result.setStatus(CreateAsyncTaskStatus.CREATE);
                        logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getTaxType().getDeclarationShortName());
                        result.setUuid(logEntryService.save(logger.getEntries()));
                        return result;
                    }
                    if (!action.isCancelTask() && declarationDataService.checkExistTask(action.getDeclarationId(), reportType, logger)) {
                        result.setStatus(CreateAsyncTaskStatus.EXIST_TASK);
                    } else if (lockDataService.lock(key, userInfo.getUser().getId(),
                            declarationDataService.getDeclarationFullName(action.getDeclarationId(), reportType),
                            LockData.State.IN_QUEUE.getText(),
                            lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA)) == null) {
                        try {
                            declarationDataService.interruptTask(action.getDeclarationId(), userInfo.getUser().getId(), reportType);
                            params.put("declarationDataId", action.getDeclarationId());
                            params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                            params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), key);
                            LockData lockData = lockDataService.getLock(key);
                            params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                            lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                            BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                            lockDataService.updateQueue(key, lockData.getDateLock(), balancingVariant);
                            logger.info(String.format(ReportType.CREATE_TASK, reportType.getDescription()), action.getTaxType().getDeclarationShortName());
                            result.setStatus(CreateAsyncTaskStatus.CREATE);
                        } catch (Exception e) {
                            lockDataService.unlock(key, userInfo.getUser().getId());
                            int i = ExceptionUtils.indexOfThrowable(e, ServiceLoggerException.class);
                            if (i != -1) {
                                throw (ServiceLoggerException)ExceptionUtils.getThrowableList(e).get(i);
                            }
                            throw new ActionException(e);
                        }
                    } else {
                        throw new ActionException("Не удалось запустить принятие. Попробуйте выполнить операцию позже");
                    }
                } else {
                    result.setStatus(CreateAsyncTaskStatus.EXIST);
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
            }
        } else {
            declarationDataService.cancel(logger, action.getDeclarationId(), securityService.currentUserInfo());
            result.setStatus(CreateAsyncTaskStatus.EXIST);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(AcceptDeclarationDataAction action, AcceptDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
