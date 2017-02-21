package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.DeleteNonVersionRefBookRowAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.DeleteNonVersionRefBookRowResult;
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
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DeleteNonVersionRefBookRowHandler extends AbstractActionHandler<DeleteNonVersionRefBookRowAction, DeleteNonVersionRefBookRowResult> {

    @Autowired
    RefBookFactory refBookFactory;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private SecurityService securityService;

    public DeleteNonVersionRefBookRowHandler() {
        super(DeleteNonVersionRefBookRowAction.class);
    }

    @Override
    public DeleteNonVersionRefBookRowResult execute(DeleteNonVersionRefBookRowAction action, ExecutionContext executionContext) throws ActionException {
        RefBookDataProvider refBookDataProvider = refBookFactory
                .getDataProvider(action.getRefBookId());

        DeleteNonVersionRefBookRowResult result = new DeleteNonVersionRefBookRowResult();
        Logger logger = new Logger();
        logger.setTaUserInfo(securityService.currentUserInfo());
        if (!action.getRecordsId().isEmpty()) {
            logger.setTaUserInfo(securityService.currentUserInfo());
            refBookDataProvider.deleteRecordVersions(logger, action.getRecordsId(), action.isOkDelete());
            if (logger.containsLevel(LogLevel.WARNING)){
                result.setWarning(true);
            }
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public void undo(DeleteNonVersionRefBookRowAction deleteNonVersionRefBookRowAction, DeleteNonVersionRefBookRowResult deleteNonVersionRefBookRowResult, ExecutionContext executionContext) throws ActionException {
    }
}
