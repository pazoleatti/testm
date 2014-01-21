package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.DeleteDTVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.DeleteDTVersionResult;
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
public class DeleteDTVersionHandler extends AbstractActionHandler<DeleteDTVersionAction, DeleteDTVersionResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    public DeleteDTVersionHandler() {
        super(DeleteDTVersionAction.class);
    }

    @Override
    public DeleteDTVersionResult execute(DeleteDTVersionAction action, ExecutionContext context) throws ActionException {
        DeleteDTVersionResult result = new DeleteDTVersionResult();
        Logger logger = new Logger();
        mainOperatingService.deleteVersionTemplate(action.getDeclarationTemplateId(), null, logger);
        result.setLogEntryUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(DeleteDTVersionAction action, DeleteDTVersionResult result, ExecutionContext context) throws ActionException {

    }
}
