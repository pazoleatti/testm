package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.InitTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.InitTypeResult;
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
public class InitTypeHandler extends AbstractActionHandler<InitTypeAction, InitTypeResult> {
    @Autowired
    private RefBookFactory refBookFactory;

    public InitTypeHandler() {
        super(InitTypeAction.class);
    }

    @Override
    public InitTypeResult execute(InitTypeAction action, ExecutionContext executionContext) throws ActionException {
        InitTypeResult result = new InitTypeResult();
        result.setRefBookList(refBookFactory.getAll(false));
        return result;
    }

    @Override
    public void undo(InitTypeAction initTypeAction, InitTypeResult initTypeResult, ExecutionContext executionContext) throws ActionException {

    }
}
