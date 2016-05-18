package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.server;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.DeleteVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.DeleteVersionResult;
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
public class DeleteVersionHandler extends AbstractActionHandler<DeleteVersionAction, DeleteVersionResult> {


    @Autowired
    @Qualifier("formTemplateMainOperatingService")
    MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService  securityService;

    @Autowired
    FormTemplateDao formTemplateDao;


    public DeleteVersionHandler() {
        super(DeleteVersionAction.class);
    }

    @Override
    public DeleteVersionResult execute(DeleteVersionAction action, ExecutionContext context) throws ActionException {
        Logger logger = new Logger();
        DeleteVersionResult result = new DeleteVersionResult();
        result.setLastVersion(mainOperatingService.deleteVersionTemplate(action.getFormTemplateId(), logger, securityService.currentUserInfo()));
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(DeleteVersionAction action, DeleteVersionResult result, ExecutionContext context) throws ActionException {

    }
}
