package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DTDeleteAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DTDeleteResult;
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
public class DTDeleteHandler extends AbstractActionHandler<DTDeleteAction, DTDeleteResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    public DTDeleteHandler() {
        super(DTDeleteAction.class);
    }

    @Override
    public DTDeleteResult execute(DTDeleteAction action, ExecutionContext context) throws ActionException {
        DTDeleteResult result = new DTDeleteResult();
        Logger logger = new Logger();
        mainOperatingService.deleteTemplate(action.getDtTypeId(), logger, securityService.currentUserInfo().getUser());
        if (!logger.getEntries().isEmpty())
            result.setLogEntriesUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(DTDeleteAction action, DTDeleteResult result, ExecutionContext context) throws ActionException {

    }
}
