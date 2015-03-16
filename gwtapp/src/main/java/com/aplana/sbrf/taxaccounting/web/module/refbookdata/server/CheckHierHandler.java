package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckHierAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckHierResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("isAuthenticated()")
public class CheckHierHandler extends AbstractActionHandler<CheckHierAction, CheckHierResult> {
    @Autowired
    private RefBookFactory refBookFactory;

    public CheckHierHandler() {
        super(CheckHierAction.class);
    }

    @Override
    public CheckHierResult execute(CheckHierAction action, ExecutionContext executionContext) throws ActionException {
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        if (!RefBookDaoImpl.checkHierarchical(refBook)) {
            throw new TaActionException(String.format(RefBookDaoImpl.NOT_HIERARCHICAL_REF_BOOK_ERROR, refBook.getName(), refBook.getId()));
        }
        return new CheckHierResult();
    }

    @Override
    public void undo(CheckHierAction checkHierAction, CheckHierResult checkHierResult, ExecutionContext executionContext) throws ActionException {

    }
}
