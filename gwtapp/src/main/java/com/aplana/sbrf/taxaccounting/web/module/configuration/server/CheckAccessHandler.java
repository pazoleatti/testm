package com.aplana.sbrf.taxaccounting.web.module.configuration.server;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.CheckAccessAction;
import com.aplana.sbrf.taxaccounting.web.module.configuration.shared.CheckAccessResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CheckAccessHandler extends AbstractActionHandler<CheckAccessAction, CheckAccessResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    LogEntryService logEntryService;

    public CheckAccessHandler() {
        super(CheckAccessAction.class);
    }

    @Override
    public CheckAccessResult execute(CheckAccessAction action, ExecutionContext context) throws ActionException {
        CheckAccessResult result = new CheckAccessResult();
        Logger logger = new Logger();
        if (action.getGroup().equals(ConfigurationParamGroup.COMMON) || action.getGroup().equals(ConfigurationParamGroup.FORM)) {
            configurationService.checkReadWriteAccess(securityService.currentUserInfo(), action.getModel(), logger);
        } else if (action.getGroup().equals(ConfigurationParamGroup.EMAIL)) {
             // TODO проверка авторизации с указанными данными
        }

        if (logger.getEntries() != null) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public void undo(CheckAccessAction action, CheckAccessResult result, ExecutionContext context) throws ActionException {
    }
}
