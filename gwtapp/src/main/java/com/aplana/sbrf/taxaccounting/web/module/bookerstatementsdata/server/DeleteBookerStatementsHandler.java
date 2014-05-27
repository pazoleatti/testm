package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.server;

import com.aplana.sbrf.taxaccounting.model.BookerStatementsType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared.DeleteBookerStatementsAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared.DeleteBookerStatementsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DeleteBookerStatementsHandler extends AbstractActionHandler<DeleteBookerStatementsAction, DeleteBookerStatementsResult> {

    @Autowired
    RefBookFactory rbFactory;

    public DeleteBookerStatementsHandler() {
        super(DeleteBookerStatementsAction.class);
    }

    @Override
    public DeleteBookerStatementsResult execute(DeleteBookerStatementsAction action, ExecutionContext context) throws ActionException {
        RefBookDataProvider provider;
        if (action.getStatementsKind() == BookerStatementsType.INCOME101.getId()) {
            provider = rbFactory.getDataProvider(GetBookerStatementsHandler.REF_BOOK_101);
        } else {
            provider = rbFactory.getDataProvider(GetBookerStatementsHandler.REF_BOOK_102);
        }
        provider.deleteRecordVersions(new Logger(), action.getUniqueRecordIds());
        return new DeleteBookerStatementsResult();
    }

    @Override
    public void undo(DeleteBookerStatementsAction action, DeleteBookerStatementsResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
