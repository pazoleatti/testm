package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeleteXsdAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeleteXsdResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class DeleteXsdHandler extends AbstractActionHandler<DeleteXsdAction, DeleteXsdResult> {

    @Autowired
    DeclarationTemplateService declarationTemplateService;

    public DeleteXsdHandler() {
        super(DeleteXsdAction.class);
    }

    @Override
    public DeleteXsdResult execute(DeleteXsdAction action, ExecutionContext context) throws ActionException {
        declarationTemplateService.deleteXsd(action.getDtId());
        return new DeleteXsdResult();
    }

    @Override
    public void undo(DeleteXsdAction action, DeleteXsdResult result, ExecutionContext context) throws ActionException {

    }
}
