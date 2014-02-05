package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.SetStatusFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.SetStatusFormResult;
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
public class SetStatusFormHandler extends AbstractActionHandler<SetStatusFormAction, SetStatusFormResult> {

    @Autowired
    @Qualifier("formTemplateMainOperatingService")
    private MainOperatingService mainOperatingService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    public SetStatusFormHandler() {
        super(SetStatusFormAction.class);
    }

    @Override
    public SetStatusFormResult execute(SetStatusFormAction action, ExecutionContext context) throws ActionException {
        SetStatusFormResult result = new SetStatusFormResult();
        Logger logger =  new Logger();
        mainOperatingService.setStatusTemplate(action.getFormTemplateId(), logger, securityService.currentUserInfo().getUser());
        result.setStatus(formTemplateService.get(action.getFormTemplateId()).getStatus().getId());
        if (!logger.getEntries().isEmpty()){
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public void undo(SetStatusFormAction action, SetStatusFormResult result, ExecutionContext context) throws ActionException {

    }
}
