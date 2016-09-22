package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.AsyncTaskManagerService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
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

import java.util.HashMap;
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
    private LockDataService lockDataService;

    @Autowired
    private AsyncTaskManagerService asyncTaskManagerService;

	public GoMoveHandler() {
		super(GoMoveAction.class);
	}

	@Override
	public GoMoveResult execute(final GoMoveAction action, ExecutionContext context)
			throws ActionException {
        final ReportType reportType = ReportType.MOVE_FD;
        final GoMoveResult result = new GoMoveResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
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
                    Pair<Boolean, String> restartStatus = asyncTaskManagerService.restartTask(keyTask, formDataService.getTaskName(reportType, action.getFormDataId(), userInfo), userInfo, action.isForce(), logger);
                    if (restartStatus != null && restartStatus.getFirst()) {
                        result.setLock(true);
                        result.setRestartMsg(restartStatus.getSecond());
                    } else if (restartStatus != null && !restartStatus.getFirst()) {
                        result.setLock(false);
                    } else {
                        result.setLock(false);
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("formDataId", action.getFormDataId());
                        params.put("workflowMoveId", action.getMove().getId());
                        asyncTaskManagerService.createTask(keyTask, reportType, params, action.isCancelTask(), PropertyLoader.isProductionMode(), userInfo, logger, new AsyncTaskHandler() {
                            @Override
                            public LockData createLock(String keyTask, ReportType reportType, TAUserInfo userInfo) {
                                return lockDataService.lock(keyTask, userInfo.getUser().getId(),
                                        formDataService.getFormDataFullName(action.getFormDataId(), false, action.getMove().isReasonToMoveShouldBeSpecified() ? String.format("Возврат налоговой формы в \"%s\"", action.getMove().getToState().getTitle()) : String.format("%s налоговой формы", action.getMove().getToState().getActionName()), reportType),
                                        LockData.State.IN_QUEUE.getText());
                            }

                            @Override
                            public void executePostCheck() {
                                result.setLockTask(true);
                            }

                            @Override
                            public boolean checkExistTask(ReportType reportType, TAUserInfo userInfo, Logger logger) {
                                return formDataService.checkExistTask(action.getFormDataId(), false, reportType, logger, userInfo);
                            }

                            @Override
                            public void interruptTask(ReportType reportType, TAUserInfo userInfo) {
                                formDataService.interruptTask(action.getFormDataId(), false, userInfo, reportType, LockDeleteCause.FORM_MOVE);
                            }

                            @Override
                            public String getTaskName(ReportType reportType, TAUserInfo userInfo) {
                                return formDataService.getTaskName(reportType, action.getFormDataId(), userInfo);
                            }
                        });
                    }
                    break;
                default:
                    if (lockDataService.lock(keyTask,
                            userInfo.getUser().getId(),
                            formDataService.getFormDataFullName(action.getFormDataId(), false, action.getMove().isReasonToMoveShouldBeSpecified() ? String.format("Возврат налоговой формы в \"%s\"", action.getMove().getToState().getTitle()) : String.format("\"%s\" налоговой формы", action.getMove().getToState().getActionName()), reportType)) == null) {
                        try {
                            formDataService.doMove(action.getFormDataId(), false, securityService.currentUserInfo(),
                                    action.getMove(), action.getReasonToWorkflowMove(), logger, false, new LockStateLogger() {
                                        @Override
                                        public void updateState(String state) {
                                        }
                                    });
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
