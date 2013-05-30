package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * 
 * @author Eugene Stetsenko Обработчик запроса для удаления формы.
 * 
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class DeleteFormDataHandler extends
		AbstractActionHandler<DeleteFormDataAction, DeleteFormDataResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	public DeleteFormDataHandler() {
		super(DeleteFormDataAction.class);
	}

	@Override
	public DeleteFormDataResult execute(DeleteFormDataAction action,
			ExecutionContext context) throws ActionException {
		formDataService.deleteFormData(securityService.getIp(), securityService.currentUser().getId(),
				action.getFormDataId());
		return new DeleteFormDataResult();
	}

	@Override
	public void undo(DeleteFormDataAction action, DeleteFormDataResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
