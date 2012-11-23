package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataWorkflowService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetAvailableMovesAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetAvailableMovesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetAvailableMovesHandler extends AbstractActionHandler<GetAvailableMovesAction, GetAvailableMovesResult> {
	
	@Autowired
	private TAUserDao userDao;
	
	@Autowired
	private FormDataWorkflowService workflowService;
	
	public GetAvailableMovesHandler() {
		super(GetAvailableMovesAction.class);
	}
	
	@Override
	public GetAvailableMovesResult execute(GetAvailableMovesAction action, ExecutionContext context) throws ActionException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String login = auth.getName();
		TAUser user = userDao.getUser(login);
		Integer userId = user.getId();
		
		List<WorkflowMove> availableMoves =	workflowService.getAvailableMoves(userId, action.getFormDataId());
		GetAvailableMovesResult result = new GetAvailableMovesResult();
		result.setAvailableMoves(availableMoves);
		return result;
	}

	@Override
	public void undo(GetAvailableMovesAction action, GetAvailableMovesResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
