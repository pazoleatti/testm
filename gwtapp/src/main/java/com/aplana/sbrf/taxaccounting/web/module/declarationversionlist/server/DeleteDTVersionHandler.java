package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.server;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.DeleteDTVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.DeleteDTVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class DeleteDTVersionHandler extends AbstractActionHandler<DeleteDTVersionAction, DeleteDTVersionResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;


    public DeleteDTVersionHandler() {
        super(DeleteDTVersionAction.class);
    }

    @Override
    public DeleteDTVersionResult execute(DeleteDTVersionAction action, ExecutionContext context) throws ActionException {
        DeleteDTVersionResult result = new DeleteDTVersionResult();
        Logger logger = new Logger();
        result.setLastVersion(mainOperatingService.deleteVersionTemplate(action.getDeclarationTemplateId(), logger, securityService.currentUserInfo()));
        result.setLogEntryUuid(logEntryService.save(logger.getEntries()));

        TemplateChanges changes = new TemplateChanges();
        changes.setEvent(FormDataEvent.TEMPLATE_DELETED);
        changes.setEventDate(new Date());
        changes.setDeclarationTemplateId(action.getDeclarationTemplateId());
        changes.setAuthor(securityService.currentUserInfo().getUser());

        return result;
    }

    @Override
    public void undo(DeleteDTVersionAction action, DeleteDTVersionResult result, ExecutionContext context) throws ActionException {

    }
}
