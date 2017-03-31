package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.ChangeStatusEDDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.ChangeStatusEDDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class ChangeStatusEDDeclarationDataHandler extends AbstractActionHandler<ChangeStatusEDDeclarationDataAction, ChangeStatusEDDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;
	@Autowired
	private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;

    public ChangeStatusEDDeclarationDataHandler() {
        super(ChangeStatusEDDeclarationDataAction.class);
    }

    @Override
    public ChangeStatusEDDeclarationDataResult execute(ChangeStatusEDDeclarationDataAction action, ExecutionContext context) {
        ChangeStatusEDDeclarationDataResult result = new ChangeStatusEDDeclarationDataResult();
        if (!declarationDataService.existDeclarationData(action.getDeclarationId())) {
            result.setExistDeclarationData(false);
            result.setDeclarationDataId(action.getDeclarationId());
            return result;
        }
        Logger logger = new Logger();
		declarationDataService.changeDocState(logger, securityService.currentUserInfo(), action.getDeclarationId(), action.getDocStateId());
        logger.info("Успешно выполнено изменение состояния ЭД");
        result.setUuid(logEntryService.save(logger.getEntries()));
	    return result;
    }

    @Override
    public void undo(ChangeStatusEDDeclarationDataAction action, ChangeStatusEDDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
