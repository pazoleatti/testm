package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTestAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormTestResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetFormTestHandler extends AbstractActionHandler<GetFormTestAction, GetFormTestResult> {

    @Autowired
    FormTemplateService formTemplateService;

    public GetFormTestHandler() {
        super(GetFormTestAction.class);
    }

    @Override
    public GetFormTestResult execute(GetFormTestAction action, ExecutionContext executionContext) throws ActionException {
        formTemplateService.executeTestScript(action.getFormTemplate());
        return new GetFormTestResult();
    }

    @Override
    public void undo(GetFormTestAction getFormTestAction, GetFormTestResult getFormTestResult, ExecutionContext executionContext) throws ActionException {

    }
}
