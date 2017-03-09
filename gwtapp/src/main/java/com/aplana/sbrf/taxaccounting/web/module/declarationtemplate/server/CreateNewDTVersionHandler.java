package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CreateNewDTVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CreateNewDTVersionResult;
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
public class CreateNewDTVersionHandler extends AbstractActionHandler<CreateNewDTVersionAction, CreateNewDTVersionResult> {

    @Autowired
    @Qualifier("declarationTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    public CreateNewDTVersionHandler() {
        super(CreateNewDTVersionAction.class);
    }

    @Override
    public CreateNewDTVersionResult execute(CreateNewDTVersionAction action, ExecutionContext executionContext) throws ActionException {
        CreateNewDTVersionResult result = new CreateNewDTVersionResult();
        Logger logger = new Logger();
        makeDates(action);

        int dtId = mainOperatingService.createNewTemplateVersion(action.getDeclarationTemplateExt().getDeclarationTemplate(),
                action.getDeclarationTemplateExt().getEndDate(), logger, securityService.currentUserInfo());

        if (!logger.getEntries().isEmpty())
            result.setLogUuid(logEntryService.save(logger.getEntries()));
        result.setDeclarationTemplateId(dtId);

        return result;
    }

    @Override
    public void undo(CreateNewDTVersionAction createNewDTVersionAction, CreateNewDTVersionResult createNewDTVersionResult, ExecutionContext executionContext) throws ActionException {

    }

    private void makeDates(CreateNewDTVersionAction action){
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
