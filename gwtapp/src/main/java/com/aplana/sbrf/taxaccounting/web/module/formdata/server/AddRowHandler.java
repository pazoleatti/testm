package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AddRowAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Хандлер обрабатывает событие добавления строки к форме. Вызывает
 * соответствующий метод сервиса, который добавляет выполняет скрипты добавления
 * строки или добавляет строку, если скриптов нет.
 * 
 * @author Vitalii Samolovskikh
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class AddRowHandler extends
		AbstractActionHandler<AddRowAction, FormDataResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	public AddRowHandler() {
		super(AddRowAction.class);
	}

	@Override
	public FormDataResult execute(AddRowAction action, ExecutionContext context)
		throws ActionException {
		FormData formData = action.getFormData();
		Logger logger = new Logger();
		formDataService.addRow(logger, securityService.currentUser()
				.getId(), formData, action.getCurrentDataRow());

		FormDataResult result = new FormDataResult();
		result.setFormData(formData);
		result.setLogEntries(logger.getEntries());
		result.setCurrentRow(action.getCurrentDataRow());
		return result;
	}

	@Override
	public void undo(AddRowAction action, FormDataResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
