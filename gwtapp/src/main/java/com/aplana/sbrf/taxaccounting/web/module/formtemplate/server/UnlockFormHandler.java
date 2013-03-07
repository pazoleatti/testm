package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.UnlockFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.UnlockFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class UnlockFormHandler extends AbstractActionHandler<UnlockFormAction, UnlockFormResult> {

	@Autowired
	private FormTemplateService formTemplateService;

	@Autowired
	private SecurityService securityService;

	public UnlockFormHandler() {
		super(UnlockFormAction.class);
	}

	@Override
	public UnlockFormResult execute(UnlockFormAction action, ExecutionContext executionContext) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		UnlockFormResult result = new UnlockFormResult();
		result.setUnlockedSuccessfully(formTemplateService.unlock(action.getFormId(), userId));

		return result;
	}

	@Override
	public void undo(UnlockFormAction unlockFormAction, UnlockFormResult unlockFormResult, ExecutionContext executionContext) throws ActionException {
		// Ничего не делаем
	}
}
