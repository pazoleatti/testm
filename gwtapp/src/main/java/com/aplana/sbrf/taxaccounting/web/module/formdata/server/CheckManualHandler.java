package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CheckManualAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.CheckManualResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckManualHandler extends AbstractActionHandler<CheckManualAction, CheckManualResult> {

    @Autowired
    private FormDataAccessService accessService;
    @Autowired
    private SecurityService securityService;

    public CheckManualHandler() {
        super(CheckManualAction.class);
    }

    @Override
    public CheckManualResult execute(CheckManualAction action, ExecutionContext context) throws ActionException {
        accessService.canCreateManual(new Logger(), securityService.currentUserInfo(), action.getFormDataId());
        return new CheckManualResult();
    }

    @Override
    public void undo(CheckManualAction action, CheckManualResult result, ExecutionContext context) throws ActionException {
        //do nothing
    }
}
