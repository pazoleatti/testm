package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.HasManualVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.HasManualVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class HasManualVersionHandler extends AbstractActionHandler<HasManualVersionAction, HasManualVersionResult> {

    @Autowired
    private FormDataService formDataService;

    public HasManualVersionHandler() {
        super(HasManualVersionAction.class);
    }

    @Override
    public HasManualVersionResult execute(HasManualVersionAction hasManualVersionAction, ExecutionContext executionContext) throws ActionException {
        HasManualVersionResult result = new HasManualVersionResult();
        result.setHasManualVersion(formDataService.existManual(hasManualVersionAction.getFormDataId()));
        return result;
    }

    @Override
    public void undo(HasManualVersionAction hasManualVersionAction, HasManualVersionResult hasManualVersionResult, ExecutionContext executionContext) throws ActionException {

    }
}
