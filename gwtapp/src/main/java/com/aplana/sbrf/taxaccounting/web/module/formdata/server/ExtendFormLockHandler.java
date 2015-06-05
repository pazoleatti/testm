package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ExtendFormLockAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.ExtendFormLockResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class ExtendFormLockHandler extends AbstractActionHandler<ExtendFormLockAction, ExtendFormLockResult> {

    public ExtendFormLockHandler() {
        super(ExtendFormLockAction.class);
    }

    @Autowired
    LockDataService lockService;

    @Autowired
    SecurityService securityService;

    @Autowired
    FormDataService formDataService;

    @Override
    public ExtendFormLockResult execute(ExtendFormLockAction extendFormLockAction, ExecutionContext executionContext) throws ActionException {
        TAUser currentUser = securityService.currentUserInfo().getUser();

        lockService.extend(
                formDataService.generateTaskKey(extendFormLockAction.getFormDataId(), ReportType.EDIT_FD),
                currentUser.getId(),
                lockService.getLockTimeout(LockData.LockObjects.FORM_DATA)
        );
        return new ExtendFormLockResult();
    }

    @Override
    public void undo(ExtendFormLockAction extendFormLockAction, ExtendFormLockResult extendFormLockResult, ExecutionContext executionContext) throws ActionException {

    }
}
