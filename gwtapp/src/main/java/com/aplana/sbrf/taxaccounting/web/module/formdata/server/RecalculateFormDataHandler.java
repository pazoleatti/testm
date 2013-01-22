package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Eugene Stetsenko
 *         Обработчик запроса для пересчета формы.
 */
@Service
public class RecalculateFormDataHandler extends AbstractActionHandler<RecalculateFormDataAction, FormDataResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	public RecalculateFormDataHandler() {
		super(RecalculateFormDataAction.class);
	}

	@Override
	public FormDataResult execute(RecalculateFormDataAction action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		Logger logger = new Logger();
		FormData formData = action.getFormData();
		formDataService.doCalc(logger, userId, formData);
		FormDataResult result = new FormDataResult();
		result.setLogEntries(logger.getEntries());
		result.setFormData(formData);
		return result;
	}

	@Override
	public void undo(RecalculateFormDataAction action, FormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
