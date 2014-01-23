package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetForNewFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetForNewFormResult;
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
public class GetForNewFormHandler extends AbstractActionHandler<GetForNewFormAction, GetForNewFormResult> {
    @Autowired
    private RefBookFactory refBookFactory;

    public GetForNewFormHandler() {
        super(GetForNewFormAction.class);
    }

    @Override
    public GetForNewFormResult execute(GetForNewFormAction action, ExecutionContext context) throws ActionException {
        GetForNewFormResult result = new GetForNewFormResult();
        result.setRefBookList(refBookFactory.getAll(false, null));
        return result;
    }

    @Override
    public void undo(GetForNewFormAction action, GetForNewFormResult result, ExecutionContext context) throws ActionException {

    }
}
