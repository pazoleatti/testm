package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
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
            LockData lockDataTask = lockDataService.lock(keyTask,
                    userInfo.getUser().getId(),
                    formDataService.getFormDataFullName(action.getFormDataId(), null, reportType),
                    lockDataService.getLockTimeout(LockData.LockObjects.FORM_DATA));
            if (lockDataTask == null) {
                formDataService.doMove(action.getFormDataId(), false, securityService.currentUserInfo(),
                        action.getMove(), action.getReasonToWorkflowMove(), logger);
            } else {
                throw new ActionException("Не удалось выполнить переход между этапами. Попробуйте выполнить операцию позже");
            }
        } else {
            formDataService.locked(lockType.getSecond(), logger);
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
