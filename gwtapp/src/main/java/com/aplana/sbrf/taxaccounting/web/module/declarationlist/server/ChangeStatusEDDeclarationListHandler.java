package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.ChangeStatusEDDeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.ChangeStatusEDDeclarationListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class ChangeStatusEDDeclarationListHandler extends AbstractActionHandler<ChangeStatusEDDeclarationListAction, ChangeStatusEDDeclarationListResult> {
	@Autowired
	private DeclarationDataService declarationDataService;
	@Autowired
	private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;

    public ChangeStatusEDDeclarationListHandler() {
        super(ChangeStatusEDDeclarationListAction.class);
    }

    @Override
    public ChangeStatusEDDeclarationListResult execute(ChangeStatusEDDeclarationListAction action, ExecutionContext context) {
        ChangeStatusEDDeclarationListResult result = new ChangeStatusEDDeclarationListResult();
        TAUserInfo userInfo = securityService.currentUserInfo();
        Logger logger = new Logger();
        for (Long declarationId: action.getDeclarationIds()) {
            if (declarationDataService.existDeclarationData(declarationId)) {
                Logger localLogger = new Logger();
                String declarationFullName = declarationDataService.getDeclarationFullName(declarationId, DeclarationDataReportType.DELETE_DEC);
                try {
                    declarationDataService.changeDocState(localLogger, userInfo, declarationId, action.getDocStateId());
                    logger.info("Успешно выполнено изменение состояния ЭД %s", declarationFullName);
                } catch (Exception e) {
                    logger.error("При изменение состояния ЭД %s возникли ошибки:", declarationFullName);
                } finally {
                    logger.getEntries().addAll(localLogger.getEntries());
                }
            } else {
                logger.warn(DeclarationDataDao.DECLARATION_NOT_FOUND_MESSAGE, declarationId);
            }
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
	    return result;
    }

    @Override
    public void undo(ChangeStatusEDDeclarationListAction action, ChangeStatusEDDeclarationListResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
