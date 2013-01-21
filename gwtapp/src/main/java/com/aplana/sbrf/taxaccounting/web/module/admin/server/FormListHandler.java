package com.aplana.sbrf.taxaccounting.web.module.admin.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.FormListAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.FormListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Get all form types.
 *
 * @author Vitalii Samolovskikh
 */
@Service
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
