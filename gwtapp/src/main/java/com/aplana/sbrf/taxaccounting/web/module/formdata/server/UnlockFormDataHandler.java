package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.UnlockFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class UnlockFormDataHandler extends AbstractActionHandler<UnlockFormData, UnlockFormDataResult> {

	@Autowired
	private FormDataService formDataService;

	@Autowired
	private SecurityService securityService;

	public UnlockFormDataHandler() {
		super(UnlockFormData.class);
	}

	@Override
	public UnlockFormDataResult execute(UnlockFormData action, ExecutionContext executionContext) throws ActionException {
		UnlockFormDataResult result = new UnlockFormDataResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        String uuid = UUID.randomUUID().toString();
		try{
            System.out.printf("UnlockFormDataHandler %s:: formDataId: %s, action: %s\n", uuid, action.getFormId(), action.getMsg());
            LockData lockDataEdit = formDataService.getObjectLock(action.getFormId(), userInfo);
            if (lockDataEdit != null && lockDataEdit.getUserId() == userInfo.getUser().getId()) {
                // Если есть блокировка, то удаляем задачи и откатываем изменения
                if (action.isPerformerLock()) {
                    formDataService.unlock(action.getFormId(), userInfo);
                } else if (!action.isReadOnlyMode()) {
                    System.out.printf("UnlockFormDataHandler %s:: formDataId: %s, unlock %s\n", uuid, action.getFormId(), action.getMsg());
                    formDataService.restoreCheckPoint(action.getFormId(), action.isManual(), userInfo);
                }
            }
		} catch (Exception e){
			//
		}
		return result;
	}

	@Override
	public void undo(UnlockFormData unlockFormData, UnlockFormDataResult unlockFormDataResult, ExecutionContext executionContext) throws ActionException {
		// Ничего не делаем
	}
}
