package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.MainOperatingService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.DeleteFormTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.DeleteFormTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class DeleteFormTypeHandler extends AbstractActionHandler<DeleteFormTypeAction, DeleteFormTypeResult> {

    @Autowired
    private MainOperatingService mainOperatingService;

    @Autowired
    private LogEntryService logEntryService;

    public DeleteFormTypeHandler() {
        super(DeleteFormTypeAction.class);
    }

    @Override
    public DeleteFormTypeResult execute(DeleteFormTypeAction action, ExecutionContext context) throws ActionException {
        DeleteFormTypeResult result = new DeleteFormTypeResult();
        Logger logger = new Logger();
        mainOperatingService.deleteTemplate(action.getFormTypeId(), logger);
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(DeleteFormTypeAction action, DeleteFormTypeResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
