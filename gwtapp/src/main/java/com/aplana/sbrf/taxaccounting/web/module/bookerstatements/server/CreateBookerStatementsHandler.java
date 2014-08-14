package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.CreateBookerStatementsAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.CreateBookerStatementsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lhaziev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateBookerStatementsHandler extends AbstractActionHandler<CreateBookerStatementsAction, CreateBookerStatementsResult> {

    @Autowired
    private BookerStatementsService bookerStatementsService;

    @Autowired
    SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    RefBookFactory rbFactory;

    public CreateBookerStatementsHandler() {
        super(CreateBookerStatementsAction.class);
    }

    @Override
    public CreateBookerStatementsResult execute(CreateBookerStatementsAction action, ExecutionContext context) throws ActionException {
        CreateBookerStatementsResult result = new CreateBookerStatementsResult();
        Logger logger = new Logger();
        logger.setTaUserInfo(securityService.currentUserInfo());
        bookerStatementsService.create(logger, action.getYear(), action.getAccountPeriodId(), action.getBookerStatementsTypeId(), action.getDepartmentId(), securityService.currentUserInfo());
        result.setUuid(logEntryService.save(logger.getEntries()));

        List<Long> ids = rbFactory.getDataProvider(107L).getUniqueRecordIds(null,
                " account_period_id = " + action.getAccountPeriodId() + " and year = " + action.getYear() + " and department_id = " + action.getDepartmentId());
        result.setAccountPeriodId(ids.get(0));

        if (logger.containsLevel(LogLevel.ERROR)) {
            result.setHasError(true);
        }
        return result;
    }

    @Override
    public void undo(CreateBookerStatementsAction action, CreateBookerStatementsResult result, ExecutionContext context) throws ActionException {

    }
}
