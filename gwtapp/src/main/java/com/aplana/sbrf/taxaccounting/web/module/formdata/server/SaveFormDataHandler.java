package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class SaveFormDataHandler extends
		AbstractActionHandler<SaveFormDataAction, DataRowResult> {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private FormDataService formDataService;
	@Autowired
	private DataRowService dataRowService;

	public SaveFormDataHandler() {
		super(SaveFormDataAction.class);
	}

	@Override
	public DataRowResult execute(SaveFormDataAction action,
			ExecutionContext context) throws ActionException {
		Logger logger = new Logger();
		FormData formData = action.getFormData();
		if (!action.getModifiedRows().isEmpty()) {
			dataRowService.update(securityService.currentUserInfo(), formData.getId(), action.getModifiedRows());
		}
		formDataService.saveFormData(logger, securityService.currentUserInfo(), formData);

		logger.info("Данные успешно записаны");
		DataRowResult result = new DataRowResult();
		result.setLogEntries(logger.getEntries());
		return result;
	}

	@Override
	public void undo(SaveFormDataAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
