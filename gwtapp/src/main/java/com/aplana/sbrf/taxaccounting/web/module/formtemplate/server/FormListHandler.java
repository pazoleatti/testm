package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormListAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Get all form types.
 *
 * @author Vitalii Samolovskikh
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class FormListHandler extends AbstractActionHandler<FormListAction, FormListResult> {
	@Autowired
	private FormTemplateService formTemplateService;

    public FormListHandler() {
        super(FormListAction.class);
    }

    @Override
    public FormListResult execute(FormListAction formListAction, ExecutionContext executionContext) throws ActionException {
        FormListResult result = new FormListResult();
        result.setForms(formTemplateService.listAll());
        return result;
    }

    @Override
    public void undo(FormListAction formListAction, FormListResult formListResult, ExecutionContext executionContext) throws ActionException {
        // Nothing!!!
    }
}
