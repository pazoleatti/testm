package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class SaveFormDataHandler extends
		AbstractActionHandler<SaveFormDataAction, FormDataResult> {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private FormDataService formDataService;

	public SaveFormDataHandler() {
		super(SaveFormDataAction.class);
	}

	@Override
	public FormDataResult execute(SaveFormDataAction action,
			ExecutionContext context) throws ActionException {
		Logger logger = new Logger();
		FormData formData = action.getFormData();
		TAUser currentUser = securityService.currentUser();
		formDataService.saveFormData(logger, securityService.getIp(), currentUser.getId(), formData);

		logger.info("Данные успешно записаны");
		FormDataResult result = new FormDataResult();
		result.setFormData(formData);
		result.setLogEntries(logger.getEntries());
		return result;
	}

	@Override
	public void undo(SaveFormDataAction action, FormDataResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
