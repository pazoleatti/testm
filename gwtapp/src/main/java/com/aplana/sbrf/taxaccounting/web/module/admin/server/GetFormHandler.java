package com.aplana.sbrf.taxaccounting.web.module.admin.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author Vitalii Samolovskikh
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetFormHandler extends AbstractActionHandler<GetFormAction, GetFormResult> {
    @Autowired
	private FormTemplateService formTemplateService;

	@Autowired
	private SecurityService securityService;

    public GetFormHandler() {
        super(GetFormAction.class);
    }

    @Override
    public GetFormResult execute(GetFormAction action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

        GetFormResult result = new GetFormResult();
		formTemplateService.checkLockedByAnotherUser(action.getId(), userId);
		FormTemplate formTemplate = formTemplateService.get(action.getId());
		formTemplateService.lock(action.getId(), userId);
		result.setForm(formTemplate);
        return result;
    }

    @Override
    public void undo(GetFormAction action, GetFormResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
