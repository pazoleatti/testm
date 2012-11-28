package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataWorkflowService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * 
 * @author Eugene Stetsenko
 * Обработчик запроса для перехода между этапами.
 *
 */
@Service
public class GoMoveHandler extends AbstractActionHandler<GoMoveAction, GoMoveResult> {

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private FormDataWorkflowService workflowService;
	
	public GoMoveHandler() {
		super(GoMoveAction.class);
	}
	
	@Override
	public GoMoveResult execute(GoMoveAction action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		workflowService.doMove(action.getFormDataId(), userId, action.getMove());
		return new GoMoveResult();
	}

	@Override
	public void undo(GoMoveAction action, GoMoveResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
