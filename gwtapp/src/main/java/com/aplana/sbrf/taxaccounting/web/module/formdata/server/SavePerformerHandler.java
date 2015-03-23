package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SavePerformerAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SavePerformerResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * 
 * @author lhaziev
 * 
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SavePerformerHandler extends AbstractActionHandler<SavePerformerAction, SavePerformerResult> {

	@Autowired
	private SecurityService securityService;

	@Autowired
	private FormDataService formDataService;

    @Autowired
    private LogEntryService logEntryService;

	public SavePerformerHandler() {
		super(SavePerformerAction.class);
	}

	@Override
	public SavePerformerResult execute(SavePerformerAction action, ExecutionContext context)
        throws ActionException {
        SavePerformerResult result = new SavePerformerResult();
        Logger logger = new Logger();
        formDataService.savePerformer(logger, securityService.currentUserInfo(), action.getFormData());
        if (!logger.getEntries().isEmpty())
            result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
	}

	@Override
	public void undo(SavePerformerAction action, SavePerformerResult result,
			ExecutionContext context) throws ActionException {
		// Ничего не делаем
	}
}
