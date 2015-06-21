package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.async.manager.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Eugene Stetsenko Обработчик запроса для перехода между этапами.
 * 
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GoMoveHandler extends AbstractActionHandler<GoMoveAction, GoMoveResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private TAUserService userService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm z");

	public GoMoveHandler() {
		super(GoMoveAction.class);
	}

	@Override
	public GoMoveResult execute(GoMoveAction action, ExecutionContext context)
			throws ActionException {
        final ReportType reportType = ReportType.MOVE_FD;
        TAUserInfo userInfo = securityService.currentUserInfo();
        GoMoveResult result = new GoMoveResult();
        Logger logger = new Logger();
        Pair<ReportType, LockData> lockType = formDataService.getLockTaskType(action.getFormDataId());
        if (lockType == null || reportType.equals(lockType.getFirst())) {
            String keyTask = formDataService.generateTaskKey(action.getFormDataId(), reportType);
            switch (action.getMove().getEvent()) {
                case MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
                case MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
                case MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
                case MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
                case MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
                case MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
                    LockData lockDataTask = lockDataService.getLock(keyTask);
                    if (lockDataTask != null && lockDataTask.getUserId() == userInfo.getUser().getId()) {
                        if (action.isForce()) {
                            // Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                            lockDataService.interruptTask(lockDataTask, userInfo.getUser().getId(), false);
                        } else {
                            result.setLock(true);
                            String restartMsg = (lockType.getSecond().getState().equals(LockData.State.IN_QUEUE.getText())) ?
                                    String.format(LockData.CANCEL_MSG, formDataService.getTaskName(reportType, action.getFormDataId(), userInfo)) :
                                    String.format(LockData.RESTART_MSG, formDataService.getTaskName(reportType, action.getFormDataId(), userInfo));
                            result.setRestartMsg(restartMsg);
                            return result;
                        }
                    } else if (lockDataTask != null) {
                        try {
                            lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                            logger.info(String.format(LockData.LOCK_INFO_MSG,
                                    formDataService.getTaskName(reportType, action.getFormDataId(), userInfo),
                                    sdf.format(lockDataTask.getDateLock()),
                                    userService.getUser(lockDataTask.getUserId()).getName()));
                        } catch (ServiceException e) {
                        }
                        result.setLock(false);
                        logger.info(String.format(ReportType.CREATE_TASK, formDataService.getTaskName(reportType, action.getFormDataId(), userInfo)));
                        result.setUuid(logEntryService.save(logger.getEntries()));
                        return result;
                    }
                    if (!action.isCancelTask() && formDataService.checkExistTask(action.getFormDataId(), false, reportType, logger, userInfo)) {
                        result.setLockTask(true);
                    } else if (lockDataService.lock(keyTask, userInfo.getUser().getId(),
                            formDataService.getFormDataFullName(action.getFormDataId(), action.getMove().isReasonToMoveShouldBeSpecified() ? action.getMove().getToState().getName() : null, reportType),
                            LockData.State.IN_QUEUE.getText(),
                            lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA)) == null) {
                        try {
                            formDataService.interruptTask(action.getFormDataId(), false, userInfo.getUser().getId(), reportType);
                            Map<String, Object> params = new HashMap<String, Object>();
                            params.put("formDataId", action.getFormDataId());
                            params.put("workflowMoveId", action.getMove().getId());
                            params.put(AsyncTask.RequiredParams.USER_ID.name(), userInfo.getUser().getId());
                            params.put(AsyncTask.RequiredParams.LOCKED_OBJECT.name(), keyTask);
                            LockData lockData = lockDataService.getLock(keyTask);
                            params.put(AsyncTask.RequiredParams.LOCK_DATE.name(), lockData.getDateLock());
                            lockDataService.addUserWaitingForLock(keyTask, userInfo.getUser().getId());
                            BalancingVariants balancingVariant = asyncManager.executeAsync(reportType.getAsyncTaskTypeId(PropertyLoader.isProductionMode()), params);
                            lockDataService.updateQueue(keyTask, lockData.getDateLock(), balancingVariant);
                            logger.info(String.format(ReportType.CREATE_TASK, formDataService.getTaskName(reportType, action.getFormDataId(), userInfo)));
                            result.setLock(false);
                        } catch (Exception e) {
                            lockDataService.unlock(keyTask, userInfo.getUser().getId());
                            int i = ExceptionUtils.indexOfThrowable(e, ServiceLoggerException.class);
                            if (i != -1) {
                                throw (ServiceLoggerException)ExceptionUtils.getThrowableList(e).get(i);
                            }
                            throw new ActionException(e);
                        }
                    } else {
                        throw new ActionException("Не удалось выполнить переход между этапами. Попробуйте выполнить операцию позже");
                    }
                    break;
                default:
                    if (lockDataService.lock(keyTask,
                            userInfo.getUser().getId(),
                            formDataService.getFormDataFullName(action.getFormDataId(), action.getMove().getToState().getActionName(), reportType),
                            lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA)) == null) {
                        try {
                            formDataService.doMove(action.getFormDataId(), false, securityService.currentUserInfo(),
                                    action.getMove(), action.getReasonToWorkflowMove(), logger, false);
                        } finally {
                            lockDataService.unlock(keyTask, userInfo.getUser().getId());
                        }
                    } else {
                        throw new ActionException("Не удалось выполнить переход между этапами. Попробуйте выполнить операцию позже");
                    }
            }
        } else {
            formDataService.locked(action.getFormDataId(), reportType, lockType, logger);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
	}

	@Override
	public void undo(GoMoveAction action, GoMoveResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
