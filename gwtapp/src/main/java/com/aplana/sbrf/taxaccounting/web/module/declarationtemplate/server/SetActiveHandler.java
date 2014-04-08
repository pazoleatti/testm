package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.SetActiveAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.SetActiveResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class SetActiveHandler extends AbstractActionHandler<SetActiveAction, SetActiveResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public SetActiveHandler() {
        super(SetActiveAction.class);
    }

    @Override
    public SetActiveResult execute(SetActiveAction action, ExecutionContext context) throws ActionException {
        SetActiveResult result = new SetActiveResult();
        Logger logger = new Logger();
        result.setIsSetActiveSuccessfully(mainOperatingService.setStatusTemplate(action.getDtId(), logger, securityService.currentUserInfo().getUser(), action.getForce()));
        if (!logger.getEntries().isEmpty())
            result.setUuid(logEntryService.save(logger.getEntries()));
        result.setStatus(declarationTemplateService.get(action.getDtId()).getStatus().getId());
        /*if (logger.containsLevel(LogLevel.ERROR)){
            throw new ServiceLoggerException("Найдены экземпляры налоговых форм", logEntryService.save(logger.getEntries()));
        }*/
        return result;
    }

    @Override
    public void undo(SetActiveAction action, SetActiveResult result, ExecutionContext context) throws ActionException {

    }
}
