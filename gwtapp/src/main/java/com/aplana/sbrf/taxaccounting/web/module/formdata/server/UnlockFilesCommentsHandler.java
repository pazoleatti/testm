package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFilesCommentsAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFilesCommentsResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class UnlockFilesCommentsHandler extends AbstractActionHandler<UnlockFilesCommentsAction, UnlockFilesCommentsResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LockDataService lockService;

	public UnlockFilesCommentsHandler() {
		super(UnlockFilesCommentsAction.class);
	}

	@Override
	public UnlockFilesCommentsResult execute(UnlockFilesCommentsAction action, ExecutionContext executionContext) throws ActionException {
        UnlockFilesCommentsResult result = new UnlockFilesCommentsResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
		try{
            String key = formDataService.generateTaskKey(action.getFormId(), ReportType.EDIT_FILE_COMMENT);
            LockData lock = lockService.getLock(key);
            if (lock != null && lock.getUserId() == userInfo.getUser().getId()) {
                lockService.unlock(key, userInfo.getUser().getId());
            }
		} catch (Exception e){
			//
		}
		return result;
	}

	@Override
	public void undo(UnlockFilesCommentsAction action, UnlockFilesCommentsResult result, ExecutionContext executionContext) throws ActionException {
		// Ничего не делаем
	}
}
