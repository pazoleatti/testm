package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class UnlockFormDataHandler extends AbstractActionHandler<UnlockFormData, UnlockFormDataResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	public UnlockFormDataHandler() {
		super(UnlockFormData.class);
	}

	@Override
	public UnlockFormDataResult execute(UnlockFormData action, ExecutionContext executionContext) throws ActionException {
		UnlockFormDataResult result = new UnlockFormDataResult();
		result.setUnlockedSuccessfully(formDataService.unlock(action.getFormId(), securityService.currentUserInfo()));

		return result;
	}

	@Override
	public void undo(UnlockFormData unlockFormData, UnlockFormDataResult unlockFormDataResult, ExecutionContext executionContext) throws ActionException {
		// Ничего не делаем
	}
}
