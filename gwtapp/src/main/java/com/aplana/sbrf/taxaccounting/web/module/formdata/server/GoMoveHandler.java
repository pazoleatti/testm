package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GoMoveResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import java.util.Date;

/**
 * 
 * @author Eugene Stetsenko Обработчик запроса для перехода между этапами.
 * 
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GoMoveHandler extends
		AbstractActionHandler<GoMoveAction, GoMoveResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private LogBusinessService logBusinessService;

	public GoMoveHandler() {
		super(GoMoveAction.class);
	}

	@Override
	public GoMoveResult execute(GoMoveAction action, ExecutionContext context)
		throws ActionException {

		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		Logger logger = new Logger();
		formDataService.doMove(action.getFormDataId(), userId,
				action.getMove(), logger);

		LogBusiness log = new LogBusiness();
		log.setFormId(action.getFormDataId());

		// TODO: необходимо сделать конвертор из workflow в formDataEvent
		log.setEventId(action.getMove().getId());
		log.setUserId(userId);
		log.setLogDate(new Date());
		log.setNote("супер заметка");

		StringBuilder roles = new StringBuilder();
		for (TARole role : user.getRoles()) {
			roles.append(role.getName());
		}
		log.setRoles(roles.toString());

		logBusinessService.add(log);

		GoMoveResult result = new GoMoveResult();
		result.setLogEntries(logger.getEntries());
		return result;

	}

	@Override
	public void undo(GoMoveAction action, GoMoveResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}

}
