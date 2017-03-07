package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.EditDeclarationTypeNameAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.EditDeclarationTypeNameResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class EditDeclarationTypeNameHandler extends AbstractActionHandler<EditDeclarationTypeNameAction, EditDeclarationTypeNameResult> {

    public EditDeclarationTypeNameHandler() {
        super(EditDeclarationTypeNameAction.class);
    }

    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Override
    public EditDeclarationTypeNameResult execute(EditDeclarationTypeNameAction action, ExecutionContext executionContext) throws ActionException {
        declarationTypeService.updateDT(action.getNewDeclarationType());
        return new EditDeclarationTypeNameResult();
    }

    @Override
    public void undo(EditDeclarationTypeNameAction editDeclarationTypeNameAction, EditDeclarationTypeNameResult editDeclarationTypeNameResult, ExecutionContext executionContext) throws ActionException {

    }
}
