package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
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
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
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
public class CheckDeclarationDataHandler extends AbstractActionHandler<CheckDeclarationDataAction, CheckDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private TAUserService userService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm z");

    public CheckDeclarationDataHandler() {
        super(CheckDeclarationDataAction.class);
    }

    @Override
    public CheckDeclarationDataResult execute(CheckDeclarationDataAction action, ExecutionContext context) throws ActionException {
        final ReportType reportType = ReportType.CHECK_DEC;
		CheckDeclarationDataResult result = new CheckDeclarationDataResult();
        Map<String, Object> params = new HashMap<String, Object>();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        LockData lockDataAccept = lockDataService.getLock(declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), ReportType.ACCEPT_DEC));
        if (lockDataAccept == null) {
            String uuidXml = reportService.getDec(userInfo, action.getDeclarationId(), ReportType.XML_DEC);
            if (uuidXml != null) {
                String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), reportType);
                LockData lockDataReportTask = lockDataService.getLock(key);
                if (lockDataReportTask != null && lockDataReportTask.getUserId() == userInfo.getUser().getId()) {
                    if (action.isForce()) {
                        // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                        lockDataService.interruptTask(lockDataReportTask, userInfo.getUser().getId(), false);
                    } else {
                        result.setStatus(CreateAsyncTaskStatus.LOCKED);
                        String restartMsg = (lockDataReportTask.getState().equals(LockData.State.IN_QUEUE.getText())) ?
                                String.format(LockData.CANCEL_MSG, String.format(ReportType.CHECK_DEC.getDescription(), action.getTaxType().getDeclarationShortName())) :
                                String.format(LockData.RESTART_MSG, String.format(ReportType.CHECK_DEC.getDescription(), action.getTaxType().getDeclarationShortName()));
                        result.setRestartMsg(restartMsg);
                        return result;
                    }
                }
                if ((lockDataReportTask = lockDataService.lock(key, userInfo.getUser().getId(),
                        declarationDataService.getDeclarationFullName(action.getDeclarationId(), reportType),
                        LockData.State.IN_QUEUE.getText(),
                        lockDataService.getLockTimeout(LockData.LockObjects.DECLARATION_DATA))) == null) {
                    try {
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
                    try {
                        lockDataService.addUserWaitingForLock(key, userInfo.getUser().getId());
                        logger.info(String.format(LockData.LOCK_INFO_MSG,
                                String.format(ReportType.CHECK_DEC.getDescription(), action.getTaxType().getDeclarationShortName()),
                                sdf.format(lockDataReportTask.getDateLock()),
                                userService.getUser(lockDataReportTask.getUserId()).getName()));
                    } catch (ServiceException e) {
                    }
                    result.setStatus(CreateAsyncTaskStatus.CREATE);
                    result.setUuid(logEntryService.save(logger.getEntries()));
                    return result;
                }
            } else {
                result.setStatus(CreateAsyncTaskStatus.NOT_EXIST_XML);
            }
        } else {
            try {
                lockDataService.addUserWaitingForLock(lockDataAccept.getKey(), userInfo.getUser().getId());
            } catch (Exception e) {
            }
            logger.error(
                    String.format(
                            LockData.LOCK_CURRENT,
                            sdf.format(lockDataAccept.getDateLock()),
                            userService.getUser(lockDataAccept.getUserId()).getName(),
                            String.format(ReportType.ACCEPT_DEC.getDescription(), action.getTaxType().getDeclarationShortName()))
            );
            throw new ServiceLoggerException("Для текущего экземпляра %s запущена операция, при которой ее проверка невозможна", logEntryService.save(logger.getEntries()), action.getTaxType().getDeclarationShortName());
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
	    return result;
    }

    @Override
    public void undo(CheckDeclarationDataAction action, CheckDeclarationDataResult result, ExecutionContext context)
			throws ActionException {
        // Nothing!
    }
}
