package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
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
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class UpdateDeclarationHandler extends AbstractActionHandler<UpdateDeclarationAction, UpdateDeclarationResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private DeclarationTemplateService declarationTemplateService;

    public UpdateDeclarationHandler() {
        super(UpdateDeclarationAction.class);
    }

    @Override
    public UpdateDeclarationResult execute(UpdateDeclarationAction action, ExecutionContext context) {
        TAUserInfo userInfo = securityService.currentUserInfo();
		UpdateDeclarationResult result = new UpdateDeclarationResult();

        Logger logger = new Logger();
        makeDates(action);
        if (mainOperatingService.edit(action.getDeclarationTemplateExt().getDeclarationTemplate(),
                action.getDeclarationTemplateExt().getEndDate(), logger, securityService.currentUserInfo(), action.getForce())){
            int dtId = action.getDeclarationTemplateExt().getDeclarationTemplate().getId();
            declarationTemplateService.checkLockedByAnotherUser(dtId, userInfo);
            declarationTemplateService.lock(dtId, userInfo);
            if (!logger.getEntries().isEmpty())
                result.setLogUuid(logEntryService.save(logger.getEntries()));
            result.setDeclarationTemplateId(dtId);
        } else {
            result.setConfirmNeeded(true);
        }

        return result;
    }

    @Override
    public void undo(UpdateDeclarationAction action, UpdateDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
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
