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
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteRowAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Хандлер обрабатывает событие удаления строки на форме. Вызывает
 * соответствующий метод сервиса, который добавляет выполняет скрипты удаления
 * строки или удаляет строку, если скриптов нет.
 * 
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class DeleteRowHandler extends AbstractActionHandler<DeleteRowAction, DataRowResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private DataRowService dataRowService;
	
	public DeleteRowHandler() {
		super(DeleteRowAction.class);
	}

	@Override
	public DataRowResult execute(DeleteRowAction action,
			ExecutionContext context) throws ActionException {
		Logger logger = new Logger();
		FormData formData = action.getFormData();
		if (!action.getModifiedRows().isEmpty()) {
			TAUserInfo userInfo = securityService.currentUserInfo();
			dataRowService.update(userInfo, formData.getId(), action.getModifiedRows());
		}
		formDataService.deleteRow(logger, securityService.currentUserInfo(), formData, action.getCurrentDataRow());
		
		DataRowResult result = new DataRowResult();
		result.setLogEntries(logger.getEntries());
		
		return result;
	}

	@Override
	public void undo(DeleteRowAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
		
	}

}
