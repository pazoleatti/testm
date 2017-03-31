package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DeleteDeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DeleteDeclarationListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class DeleteDeclarationListHandler extends AbstractActionHandler<DeleteDeclarationListAction, DeleteDeclarationListResult> {
	@Autowired
	private DeclarationDataService declarationDataService;
	@Autowired
	private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;

    public DeleteDeclarationListHandler() {
        super(DeleteDeclarationListAction.class);
    }

    @Override
    public DeleteDeclarationListResult execute(DeleteDeclarationListAction action, ExecutionContext context) {
        DeleteDeclarationListResult result = new DeleteDeclarationListResult();
        TAUserInfo taUserInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        for(Long declarationId: action.getDeclarationIds()) {
            if (declarationDataService.existDeclarationData(declarationId)) {
                try {
                        String declarationFullName = declarationDataService.getDeclarationFullName(declarationId, null);
                        declarationDataService.delete(declarationId, taUserInfo);
                        logger.info("Успешно удалена %s.", declarationFullName);
                } catch (Exception e) {
                    logger.error("При удалении %s возникли ошибки:", declarationDataService.getDeclarationFullName(declarationId, DeclarationDataReportType.DELETE_DEC));
                    if (e instanceof ServiceLoggerException) {
                        logger.getEntries().addAll(logEntryService.getAll(((ServiceLoggerException) e).getUuid()));
                    } else {
                        logger.error(e);
                    }
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationId);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
	    return result;
    }

    @Override
    public void undo(DeleteDeclarationListAction action, DeleteDeclarationListResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
