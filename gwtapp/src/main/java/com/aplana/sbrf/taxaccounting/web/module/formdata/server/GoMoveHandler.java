package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

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
	private TAUserDao userDao;
	
	@Autowired
	private FormDataWorkflowService workflowService;
	
	public GoMoveHandler() {
		super(GoMoveAction.class);
	}
	
	@Override
	public GoMoveResult execute(GoMoveAction action, ExecutionContext context) throws ActionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String login = auth.getName();
		TAUser user = userDao.getUser(login);
		Integer userId = user.getId();
		workflowService.doMove(action.getFormDataId(), userId, action.getMove());
		GoMoveResult result = new GoMoveResult();
		return result;
	}

	@Override
	public void undo(GoMoveAction action, GoMoveResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
