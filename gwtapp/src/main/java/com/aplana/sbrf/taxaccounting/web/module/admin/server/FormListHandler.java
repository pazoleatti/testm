package com.aplana.sbrf.taxaccounting.web.module.admin.server;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.FormListAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.FormListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Get all form types.
 *
 * @author Vitalii Samolovskikh
 */
@Service
public class FormListHandler extends AbstractActionHandler<FormListAction, FormListResult> {
    private FormDao formDao;

    public FormListHandler() {
        super(FormListAction.class);
    }

    @Override
    public FormListResult execute(FormListAction formListAction, ExecutionContext executionContext) throws ActionException {
        FormListResult result = new FormListResult();
        result.setForms(formDao.listForms());
        return result;
    }

    @Override
    public void undo(FormListAction formListAction, FormListResult formListResult, ExecutionContext executionContext) throws ActionException {
        // Nothing!!!
    }

    @Autowired
    public void setFormDao(FormDao formDao) {
        this.formDao = formDao;
    }
}
