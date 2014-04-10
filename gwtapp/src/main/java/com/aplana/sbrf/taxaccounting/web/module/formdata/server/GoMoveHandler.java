package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

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

	public GoMoveHandler() {
		super(GoMoveAction.class);
	}

	@Override
	public GoMoveResult execute(GoMoveAction action, ExecutionContext context)
			throws ActionException {

			formDataService.doMove(action.getFormDataId(), false, securityService.currentUserInfo(),
					action.getMove(), action.getReasonToWorkflowMove(), new Logger());
			return new GoMoveResult();

	}

	@Override
	public void undo(GoMoveAction action, GoMoveResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
