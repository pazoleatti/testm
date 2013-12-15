package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormResult;
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
		TAUserInfo userInfo = securityService.currentUserInfo();

        GetFormResult result = new GetFormResult();
		formTemplateService.checkLockedByAnotherUser(action.getId(), userInfo);
		FormTemplate formTemplate = formTemplateService.get(action.getId());
        formTemplate.setScript(formTemplateService.getFormTemplateScript(action.getId()));
		formTemplateService.lock(action.getId(), userInfo);
		result.setForm(formTemplate);
        return result;
    }

    @Override
    public void undo(GetFormAction action, GetFormResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
