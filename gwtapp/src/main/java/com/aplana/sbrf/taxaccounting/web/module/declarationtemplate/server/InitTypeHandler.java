package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.InitTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.InitTypeResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_CONF')")
public class InitTypeHandler extends AbstractActionHandler<InitTypeAction, InitTypeResult> {
    @Autowired
    private CommonRefBookService commonRefBookService;

    public InitTypeHandler() {
        super(InitTypeAction.class);
    }

    @Override
    public InitTypeResult execute(InitTypeAction action, ExecutionContext executionContext) throws ActionException {
        InitTypeResult result = new InitTypeResult();
        result.setRefBookList(commonRefBookService.fetchAll(null));
        return result;
    }

    @Override
    public void undo(InitTypeAction initTypeAction, InitTypeResult initTypeResult, ExecutionContext executionContext) throws ActionException {

    }
}
