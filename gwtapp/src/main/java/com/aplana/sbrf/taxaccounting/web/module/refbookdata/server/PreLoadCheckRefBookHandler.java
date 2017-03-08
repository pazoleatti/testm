package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.PreLoadCheckRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.PreLoadCheckRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class PreLoadCheckRefBookHandler extends AbstractActionHandler<PreLoadCheckRefBookAction, PreLoadCheckRefBookResult> {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private LogEntryService logEntryService;

    public PreLoadCheckRefBookHandler() {
        super(PreLoadCheckRefBookAction.class);
    }

    @Override
    public PreLoadCheckRefBookResult execute(PreLoadCheckRefBookAction action, ExecutionContext context) throws ActionException {
        PreLoadCheckRefBookResult result = new PreLoadCheckRefBookResult();
        Logger logger = new Logger();
        loadRefBookDataService.preLoadCheck(action.getRefBookId(), action.getFileName(), action.getDateFrom(), action.getDateTo(), securityService.currentUserInfo(), logger);
        if (refBookFactory.get(action.getRefBookId()).isVersioned()) {
            if (action.getDateFrom() == null) {
                logger.error("Дата начала действия новых версий должна быть заполнена!");
            } else if (action.getDateTo() != null && action.getDateFrom().compareTo(action.getDateTo()) == 1) {
                logger.error("Дата начала действия новых версий должна быть не больше даты окончания!");
            }
        }
        if (logger.containsLevel(LogLevel.ERROR)) {
            result.setError(true);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(PreLoadCheckRefBookAction action, PreLoadCheckRefBookResult result, ExecutionContext context) throws ActionException {

    }
}
