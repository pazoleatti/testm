package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.CheckReadWriteAccessAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.CheckReadWriteAccessResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckReadWriteAccessHandler extends AbstractActionHandler<CheckReadWriteAccessAction, CheckReadWriteAccessResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    LogEntryService logEntryService;

    public CheckReadWriteAccessHandler() {
        super(CheckReadWriteAccessAction.class);
    }

    @Override
    public CheckReadWriteAccessResult execute(CheckReadWriteAccessAction action, ExecutionContext context) throws ActionException {
        Logger logger = new Logger();
        configurationService.checkReadWriteAccess(securityService.currentUserInfo(), action.getModel(), logger);
        CheckReadWriteAccessResult result = new CheckReadWriteAccessResult();
        if (logger.getEntries() != null) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public void undo(CheckReadWriteAccessAction action, CheckReadWriteAccessResult result, ExecutionContext context) throws ActionException {
    }
}
