package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server;


import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.InitRefBookMultiAction;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.InitRefBookMultiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author sgoryachkin
 */
@Component
@PreAuthorize("isAuthenticated()")
public class InitRefBookMultiHandler extends AbstractActionHandler<InitRefBookMultiAction, InitRefBookMultiResult> {

    @Autowired
    RefBookFactory refBookFactory;

    public InitRefBookMultiHandler() {
        super(InitRefBookMultiAction.class);
    }

    @Override
    public InitRefBookMultiResult execute(InitRefBookMultiAction action, ExecutionContext context) throws ActionException {
        InitRefBookMultiResult result = new InitRefBookMultiResult();
        RefBook refBook = refBookFactory.getByAttribute(action.getRefBookAttrId());
        result.setRefBookId(refBook.getId());
        result.setAttributes(refBook.getAttributes());
        //Признак настроек подразделений
        boolean isConfig = refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_TRANSPORT) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_INCOME) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_DEAL) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_VAT) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_PROPERTY) ||
                refBook.getId().equals(RefBook.WithTable.PROPERTY.getTableRefBookId()) ||
                refBook.getId().equals(RefBook.WithTable.TRANSPORT.getTableRefBookId()) ||
                refBook.getId().equals(RefBook.WithTable.INCOME.getTableRefBookId());
        result.setVersioned(refBook.isVersioned() && !isConfig);
        return result;
    }

    @Override
    public void undo(InitRefBookMultiAction action, InitRefBookMultiResult result, ExecutionContext context) throws ActionException {
        //
    }

}
