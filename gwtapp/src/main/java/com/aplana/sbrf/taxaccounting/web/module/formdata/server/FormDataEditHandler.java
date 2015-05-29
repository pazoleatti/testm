package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataEditAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.FormDataEditResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FormDataEditHandler extends AbstractActionHandler<FormDataEditAction, FormDataEditResult> {

    @Autowired
    private FormDataAccessService accessService;
    @Autowired
    private SecurityService securityService;
	@Autowired
	private DataRowDao dataRowDao;

    public FormDataEditHandler() {
        super(FormDataEditAction.class);
    }

    @Override
    public FormDataEditResult execute(FormDataEditAction action, ExecutionContext context) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();
		FormData formData = action.getFormData();

		accessService.canEdit(userInfo, formData.getId(), formData.isManual());
		if (formData.isManual()) {
        	accessService.canCreateManual(new Logger(), userInfo, formData.getId());
		}
		dataRowDao.createTemporary(formData);
        return new FormDataEditResult();
    }

    @Override
    public void undo(FormDataEditAction action, FormDataEditResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
