package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.api.DataRowDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
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
	private DataRowService dataRowService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private LockDataService lockDataService;

    public FormDataEditHandler() {
        super(FormDataEditAction.class);
    }

    @Override
    public FormDataEditResult execute(FormDataEditAction action, ExecutionContext context) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();
		FormData formData = action.getFormData();
        Logger logger = new Logger();
        formDataService.checkLockedByTask(formData.getId(), logger, userInfo, "Редактирование НФ", true);

        // http://conf.aplana.com/pages/viewpage.action?pageId=19664668 (2A.1)
        LockData lockDataCheck = lockDataService.getLock(formDataService.generateTaskKey(action.getFormData().getId(), ReportType.CHECK_FD));
        if(lockDataCheck != null) {
            lockDataService.interruptTask(lockDataCheck, userInfo.getUser().getId(), true);
        }

		accessService.canEdit(userInfo, formData.getId(), formData.isManual());
		if (formData.isManual()) {
        	accessService.canCreateManual(logger, userInfo, formData.getId());
		}
        return new FormDataEditResult();
    }

    @Override
    public void undo(FormDataEditAction action, FormDataEditResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
