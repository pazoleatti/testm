package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CreateNewDeclarationTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CreateNewDeclarationTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Calendar;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class CreateNewDeclarationTypeHandler extends AbstractActionHandler<CreateNewDeclarationTypeAction, CreateNewDeclarationTypeResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    public CreateNewDeclarationTypeHandler() {
        super(CreateNewDeclarationTypeAction.class);
    }

    @Override
    public CreateNewDeclarationTypeResult execute(CreateNewDeclarationTypeAction action, ExecutionContext executionContext) throws ActionException {
        CreateNewDeclarationTypeResult result= new CreateNewDeclarationTypeResult();
        Logger logger = new Logger();
        makeDates(action);
        int dtId = mainOperatingService.createNewType(action.getDeclarationTemplateExt().getDeclarationTemplate(),
                action.getDeclarationTemplateExt().getEndDate(), logger, securityService.currentUserInfo());
        if (!logger.getEntries().isEmpty())
            result.setLogUuid(logEntryService.save(logger.getEntries()));
        result.setDeclarationTemplateId(dtId);
        return result;
    }

    @Override
    public void undo(CreateNewDeclarationTypeAction createNewDeclarationTypeAction, CreateNewDeclarationTypeResult createNewDeclarationTypeResult, ExecutionContext executionContext) throws ActionException {

    }

    private void makeDates(CreateNewDeclarationTypeAction action){
        Calendar calendar = Calendar.getInstance();
        DeclarationTemplate template = action.getDeclarationTemplateExt().getDeclarationTemplate();
        calendar.setTime(template.getVersion());
        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1);
        template.setVersion(calendar.getTime());
        if (action.getDeclarationTemplateExt().getEndDate() != null){
            calendar.clear();
            calendar.setTime((action.getDeclarationTemplateExt().getEndDate()));
            calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);
            action.getDeclarationTemplateExt().setEndDate(calendar.getTime());
        }
    }
}
