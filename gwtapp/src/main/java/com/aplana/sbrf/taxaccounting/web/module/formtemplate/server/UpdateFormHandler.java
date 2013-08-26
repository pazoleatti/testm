package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.UpdateFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.UpdateFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author Vitalii Samolovskikh
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class UpdateFormHandler extends AbstractActionHandler<UpdateFormAction, UpdateFormResult> {
	@Autowired
	private FormTemplateService formTemplateService;

    public UpdateFormHandler() {
        super(UpdateFormAction.class);
    }

    @Override
    public UpdateFormResult execute(UpdateFormAction action, ExecutionContext context) {
		Logger logger = new Logger();
		UpdateFormResult result = new UpdateFormResult();

		formTemplateService.validateFormTemplate(action.getForm(), logger);
		if (logger.getEntries().isEmpty()) {
			formTemplateService.save(action.getForm());
		}

		result.setLogEntries(logger.getEntries());
		return result;
    }

    @Override
    public void undo(UpdateFormAction action, UpdateFormResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
