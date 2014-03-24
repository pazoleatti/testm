package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
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

import java.util.Calendar;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class UpdateDeclarationHandler extends AbstractActionHandler<UpdateDeclarationAction, UpdateDeclarationResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    public UpdateDeclarationHandler() {
        super(UpdateDeclarationAction.class);
    }

    @Override
    public UpdateDeclarationResult execute(UpdateDeclarationAction action, ExecutionContext context) {
		UpdateDeclarationResult result = new UpdateDeclarationResult();

        Logger logger = new Logger();
        makeDates(action);
        if (action.getDeclarationTemplateExt().getDeclarationTemplate().getId() == null
                && action.getDeclarationTemplateExt().getDeclarationTemplate().getType().getId() != 0){
            int dtId = mainOperatingService.createNewTemplateVersion(action.getDeclarationTemplateExt().getDeclarationTemplate(),
                    action.getDeclarationTemplateExt().getEndDate(), logger, securityService.currentUserInfo().getUser());
            fillResult(result, dtId, logger);

        }else if(action.getDeclarationTemplateExt().getDeclarationTemplate().getId() != null){
            int dtId = mainOperatingService.edit(action.getDeclarationTemplateExt().getDeclarationTemplate(),
                    action.getDeclarationTemplateExt().getEndDate(), logger, securityService.currentUserInfo().getUser());
            fillResult(result, dtId, logger);
        }else if (action.getDeclarationTemplateExt().getDeclarationTemplate().getId() == null &&
                action.getDeclarationTemplateExt().getDeclarationTemplate().getType().getId() == 0){
            int dtId = mainOperatingService.createNewType(action.getDeclarationTemplateExt().getDeclarationTemplate(),
                    action.getDeclarationTemplateExt().getEndDate(), logger, securityService.currentUserInfo().getUser());
            fillResult(result, dtId, logger);
        }
		return result;
    }

    @Override
    public void undo(UpdateDeclarationAction action, UpdateDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

    private void fillResult(UpdateDeclarationResult result, int dtId, Logger logger){
        if (!logger.getEntries().isEmpty())
            result.setLogUuid(logEntryService.save(logger.getEntries()));
        result.setDeclarationTemplateId(dtId);
    }

    private void makeDates(UpdateDeclarationAction action){
        Calendar calendar = Calendar.getInstance();
        DeclarationTemplate formTemplate = action.getDeclarationTemplateExt().getDeclarationTemplate();
        calendar.setTime(formTemplate.getVersion());
        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);
        formTemplate.setVersion(calendar.getTime());
        if (action.getDeclarationTemplateExt().getEndDate() != null){
            calendar.clear();
            calendar.setTime((action.getDeclarationTemplateExt().getEndDate()));
            calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
            action.getDeclarationTemplateExt().setEndDate(calendar.getTime());
        }
    }

}
