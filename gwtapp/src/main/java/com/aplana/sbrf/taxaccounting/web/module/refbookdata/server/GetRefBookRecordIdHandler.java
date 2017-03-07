package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookRecordIdAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookRecordIdResult;
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
@PreAuthorize("isAuthenticated()")
public class GetRefBookRecordIdHandler extends AbstractActionHandler<GetRefBookRecordIdAction, GetRefBookRecordIdResult> {

    @Autowired
    RefBookFactory refBookFactory;

    public GetRefBookRecordIdHandler() {
        super(GetRefBookRecordIdAction.class);
    }

    @Override
    public GetRefBookRecordIdResult execute(GetRefBookRecordIdAction action, ExecutionContext context) throws ActionException {
        GetRefBookRecordIdResult result = new GetRefBookRecordIdResult();
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(action.getRefBookId());
        result.setRecordId(refBookDataProvider.getRecordId(action.getUniqueRecordId()));
        return result;
    }

    @Override
    public void undo(GetRefBookRecordIdAction action, GetRefBookRecordIdResult result, ExecutionContext context) throws ActionException {

    }
}
