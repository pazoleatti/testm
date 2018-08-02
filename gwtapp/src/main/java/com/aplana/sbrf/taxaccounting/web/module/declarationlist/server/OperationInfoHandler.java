package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.OperationInfoAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.OperationInfoResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class OperationInfoHandler extends AbstractActionHandler<OperationInfoAction, OperationInfoResult> {

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private LogEntryService logEntryService;

    public OperationInfoHandler() {
        super(OperationInfoAction.class);
    }

    @Override
    public OperationInfoResult execute(OperationInfoAction action, ExecutionContext context) throws ActionException {
        final Logger logger = new Logger();
        for (Long declarationDataId : action.getDeclarationDataIdList()) {
            logger.info("Постановка операции \"%s\" в очередь на исполнение для объекта: %s, номер налоговой формы: %d", action.getDeclarationDataReportType(), declarationDataService.getDeclarationFullName(declarationDataId, null), declarationDataId);
        }
        OperationInfoResult result = new OperationInfoResult();
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(OperationInfoAction action, OperationInfoResult result, ExecutionContext context) throws ActionException {

    }
}
