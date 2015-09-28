package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckLinearAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.CheckLinearResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 */
@Service
public class CheckLinearHandler extends AbstractActionHandler<CheckLinearAction, CheckLinearResult> {

    @Autowired
    private RefBookFactory refBookFactory;


    public CheckLinearHandler() {
        super(CheckLinearAction.class);
    }

    @Override
    public CheckLinearResult execute(CheckLinearAction action, ExecutionContext context) throws ActionException {
        RefBook refBook = refBookFactory.get(action.getRefBookId());
        if (RefBookDaoImpl.checkHierarchical(refBook)) {
            throw new TaActionException(String.format(RefBookDaoImpl.NOT_LINEAR_REF_BOOK_ERROR, refBook.getName(), refBook.getId()));
        }
        return new CheckLinearResult();
    }

    @Override
    public void undo(CheckLinearAction action, CheckLinearResult result, ExecutionContext context) throws ActionException {

    }
}
