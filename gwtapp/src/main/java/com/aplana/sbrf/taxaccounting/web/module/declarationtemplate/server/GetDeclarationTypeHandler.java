package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationTypeResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetDeclarationTypeHandler extends AbstractActionHandler<GetDeclarationTypeAction, GetDeclarationTypeResult> {

    @Autowired
    private DeclarationTypeService declarationTypeService;

    public GetDeclarationTypeHandler() {
        super(GetDeclarationTypeAction.class);
    }

    @Override
    public GetDeclarationTypeResult execute(GetDeclarationTypeAction action, ExecutionContext context) throws ActionException {
        GetDeclarationTypeResult result = new GetDeclarationTypeResult();
        result.setDeclarationType(declarationTypeService.get(action.getDeclarationTypeId()));

        return result;
    }

    @Override
    public void undo(GetDeclarationTypeAction action, GetDeclarationTypeResult result, ExecutionContext context) throws ActionException {
    }
}
