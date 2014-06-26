package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeNameAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.EditFormTypeNameResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class EditFormTypeNameHandler extends AbstractActionHandler<EditFormTypeNameAction, EditFormTypeNameResult> {

    public EditFormTypeNameHandler() {
        super(EditFormTypeNameAction.class);
    }

    @Autowired
    private FormTypeService formTypeService;

    @Override
    public EditFormTypeNameResult execute(EditFormTypeNameAction action, ExecutionContext executionContext) throws ActionException {
        formTypeService.updateFormTypeName(action.getFormTypeId(), action.getNewFormTypeName());
        return new EditFormTypeNameResult();
    }

    @Override
    public void undo(EditFormTypeNameAction editFormTypeNameAction, EditFormTypeNameResult editFormTypeNameResult, ExecutionContext executionContext) throws ActionException {

    }
}
