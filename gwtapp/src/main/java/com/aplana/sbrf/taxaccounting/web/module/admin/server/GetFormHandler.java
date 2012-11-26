package com.aplana.sbrf.taxaccounting.web.module.admin.server;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Vitalii Samolovskikh
 */
@Service
public class GetFormHandler extends AbstractActionHandler<GetFormAction, GetFormResult> {
	@SuppressWarnings("UnusedDeclaration")
	private static final Log log = LogFactory.getLog(GetFormHandler.class);

    private FormTemplateDao formTemplateDao;

    public GetFormHandler() {
        super(GetFormAction.class);
    }

    @Override
    public GetFormResult execute(GetFormAction action, ExecutionContext context) throws ActionException {
        GetFormResult result = new GetFormResult();
		FormTemplate formTemplate = formTemplateDao.get(action.getId());
		result.setForm(formTemplate);
        return result;
    }

    @Override
    public void undo(GetFormAction action, GetFormResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

    @Autowired
    public void setFormDao(FormTemplateDao formDao) {
        this.formTemplateDao = formDao;
    }
}
