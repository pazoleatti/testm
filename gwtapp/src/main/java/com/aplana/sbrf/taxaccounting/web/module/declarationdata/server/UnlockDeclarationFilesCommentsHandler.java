package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UnlockDeclarationFilesCommentsAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UnlockDeclarationFilesCommentsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class UnlockDeclarationFilesCommentsHandler extends AbstractActionHandler<UnlockDeclarationFilesCommentsAction, UnlockDeclarationFilesCommentsResult> {

    @Autowired
    private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LockDataService lockService;

	public UnlockDeclarationFilesCommentsHandler() {
		super(UnlockDeclarationFilesCommentsAction.class);
	}

	@Override
	public UnlockDeclarationFilesCommentsResult execute(UnlockDeclarationFilesCommentsAction action, ExecutionContext executionContext) throws ActionException {
        UnlockDeclarationFilesCommentsResult result = new UnlockDeclarationFilesCommentsResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
		try{
            String key = declarationDataService.generateAsyncTaskKey(action.getDeclarationId(), DeclarationDataReportType.EDIT_FILE_COMMENT_DEC);
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
	public void undo(UnlockDeclarationFilesCommentsAction action, UnlockDeclarationFilesCommentsResult result, ExecutionContext executionContext) throws ActionException {
		// Ничего не делаем
	}
}
