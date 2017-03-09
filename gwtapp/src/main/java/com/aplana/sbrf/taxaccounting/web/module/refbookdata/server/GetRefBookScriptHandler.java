package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookScriptAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookScriptResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Fail Mukhametdinov
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetRefBookScriptHandler extends AbstractActionHandler<GetRefBookScriptAction, GetRefBookScriptResult> {

    @Autowired
    private RefBookScriptingService refBookScriptingService;

    @Autowired
    private RefBookFactory refBookFactory;

    public GetRefBookScriptHandler() {
        super(GetRefBookScriptAction.class);
    }

    @Override
    public GetRefBookScriptResult execute(GetRefBookScriptAction action, ExecutionContext context) throws ActionException {

        GetRefBookScriptResult refBookScriptResult = new GetRefBookScriptResult();

        RefBook refBook = refBookFactory.get(action.getRefBookId());

        String script = refBookScriptingService.getScript(action.getRefBookId());
        refBookScriptResult.setScript(script);
        refBookScriptResult.setName(refBook.getName());

        return refBookScriptResult;
    }

    @Override
    public void undo(GetRefBookScriptAction action, GetRefBookScriptResult result, ExecutionContext context) throws ActionException {
    }
}