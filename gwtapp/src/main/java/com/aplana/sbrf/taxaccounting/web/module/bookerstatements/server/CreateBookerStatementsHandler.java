package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BookerStatementsService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.CreateBookerStatementsAction;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.CreateBookerStatementsResult;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.server.GetBookerStatementsHandler;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public CreateBookerStatementsHandler() {
        super(CreateBookerStatementsAction.class);
    }

    @Override
    public CreateBookerStatementsResult execute(CreateBookerStatementsAction action, ExecutionContext context) throws ActionException {
        CreateBookerStatementsResult result = new CreateBookerStatementsResult();
        Logger logger = new Logger();
        bookerStatementsService.create(logger, action.getReportPeriodId(), action.getBookerStatementsTypeId(), action.getDepartmentId(), securityService.currentUserInfo());
        result.setUuid(logEntryService.save(logger.getEntries()));
        if (logger.containsLevel(LogLevel.ERROR)) {
            result.setHasError(true);
        }
        return result;
    }

    @Override
    public void undo(CreateBookerStatementsAction action, CreateBookerStatementsResult result, ExecutionContext context) throws ActionException {

    }
}
