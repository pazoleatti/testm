package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CheckJrxmlAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CheckJrxmlResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class CheckJrxmlHandler extends AbstractActionHandler<CheckJrxmlAction, CheckJrxmlResult> {

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
	private SecurityService securityService;
    @Autowired
	private LogEntryService logEntryService;

    public CheckJrxmlHandler() {
        super(CheckJrxmlAction.class);
    }

    @Override
    public CheckJrxmlResult execute(CheckJrxmlAction action, ExecutionContext context) throws ActionException {
        Logger logger = new Logger();
        logger.setTaUserInfo(securityService.currentUserInfo());

        CheckJrxmlResult result = new CheckJrxmlResult();
        result.setCanDelete(!declarationTemplateService.checkExistingDataJrxml(action.getDtId(), logger));
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(CheckJrxmlAction action, CheckJrxmlResult result, ExecutionContext context) throws ActionException {
    }
}
