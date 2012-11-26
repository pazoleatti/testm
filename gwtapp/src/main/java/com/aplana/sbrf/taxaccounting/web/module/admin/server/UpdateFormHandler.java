package com.aplana.sbrf.taxaccounting.web.module.admin.server;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.UpdateFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * @author Vitalii Samolovskikh
 */
@Service
public class UpdateFormHandler extends AbstractActionHandler<UpdateFormAction, UpdateFormResult> {
	@SuppressWarnings("UnusedDeclaration")
	private static final Logger log = Logger.getLogger(UpdateFormHandler.class.getName());
    private FormTemplateDao formTemplateDao;

    public UpdateFormHandler() {
        super(UpdateFormAction.class);
    }

    @Override
    public UpdateFormResult execute(UpdateFormAction action, ExecutionContext context) throws ActionException {
		formTemplateDao.save(action.getForm());
        return new UpdateFormResult();
    }

    @Override
    public void undo(UpdateFormAction action, UpdateFormResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

    @Autowired
    public void setFormDao(FormTemplateDao formDao) {
        this.formTemplateDao = formDao;
    }
}
