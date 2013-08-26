package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DataRowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author Eugene Stetsenko Обработчик запроса для пересчета формы.
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class RecalculateFormDataHandler extends
		AbstractActionHandler<RecalculateFormDataAction, DataRowResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DataRowService dataRowService;

	public RecalculateFormDataHandler() {
		super(RecalculateFormDataAction.class);
	}

	@Override
	public DataRowResult execute(RecalculateFormDataAction action,
			ExecutionContext context) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();
		Logger logger = new Logger();
		FormData formData = action.getFormData();
		if (!action.getModifiedRows().isEmpty()) {
			dataRowService.update(userInfo, formData.getId(), action.getModifiedRows());
		}
		formDataService.doCalc(logger, userInfo, formData);
		DataRowResult result = new DataRowResult();
		result.setLogEntries(logger.getEntries());
		return result;
	}

	@Override
	public void undo(RecalculateFormDataAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
