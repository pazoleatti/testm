package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.TemplateChangesEvent;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.UpdateDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.UpdateDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class UpdateDeclarationHandler extends AbstractActionHandler<UpdateDeclarationAction, UpdateDeclarationResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private TemplateChangesService templateChangesService;

    @Autowired
    private SecurityService securityService;

    public UpdateDeclarationHandler() {
        super(UpdateDeclarationAction.class);
    }

    @Override
    public UpdateDeclarationResult execute(UpdateDeclarationAction action, ExecutionContext context) {
		UpdateDeclarationResult result = new UpdateDeclarationResult();

        TemplateChanges changes = new TemplateChanges();

        Logger logger = new Logger();
        if (action.getDeclarationTemplateExt().getDeclarationTemplate().getId() == null && action.getDeclarationTemplateExt().getDeclarationTemplate().getType() != null){
            int dtId = mainOperatingService.createNewTemplateVersion(action.getDeclarationTemplateExt().getDeclarationTemplate(),
                    action.getDeclarationTemplateExt().getEndDate(), logger);
            result.setLogUuid(logEntryService.save(logger.getEntries()));
            result.setDeclarationTemplateId(dtId);

            changes.setEvent(TemplateChangesEvent.CREATED);
            changes.setEventDate(new Date());
            changes.setDeclarationTemplateId(dtId);
            changes.setAuthor(securityService.currentUserInfo().getUser());
            templateChangesService.save(changes);

        }else if(action.getDeclarationTemplateExt().getDeclarationTemplate().getId() != null){
            int dtId = mainOperatingService.edit(action.getDeclarationTemplateExt().getDeclarationTemplate().getId(),
                    action.getDeclarationTemplateExt().getEndDate(), logger);
            result.setLogUuid(logEntryService.save(logger.getEntries()));
            result.setDeclarationTemplateId(dtId);

            changes.setEvent(TemplateChangesEvent.MODIFIED);
            changes.setEventDate(new Date());
            changes.setDeclarationTemplateId(dtId);
            changes.setAuthor(securityService.currentUserInfo().getUser());
            templateChangesService.save(changes);
        }
		return result;
    }

    @Override
    public void undo(UpdateDeclarationAction action, UpdateDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
