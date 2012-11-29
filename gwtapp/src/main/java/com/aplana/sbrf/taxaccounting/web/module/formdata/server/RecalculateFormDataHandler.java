package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DeleteFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.RecalculateFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * 
 * @author Eugene Stetsenko
 * Обработчик запроса для пересчета формы.
 *
 */
@Service
public class RecalculateFormDataHandler extends AbstractActionHandler<RecalculateFormDataAction, RecalculateFormDataResult> {

	@Autowired
	private FormDataService formDataService;
	
	@Autowired
	private SecurityService securityService;

	public RecalculateFormDataHandler() {
		super(RecalculateFormDataAction.class);
	}
	
	@Override
	public RecalculateFormDataResult execute(RecalculateFormDataAction action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		
		FormData formData = formDataService.getFormData(userId, action.getFormDataId());
		Logger logger = new Logger();
		
		formDataService.doCalc(
				logger, 
				userId, 
				formData
				);
		RecalculateFormDataResult result = new RecalculateFormDataResult();
		result.setLogEntries(logger.getEntries());
		return result;
	}

	@Override
	public void undo(RecalculateFormDataAction action, RecalculateFormDataResult result, ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
