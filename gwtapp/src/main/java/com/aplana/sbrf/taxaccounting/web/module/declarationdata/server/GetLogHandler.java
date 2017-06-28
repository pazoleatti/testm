package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetLogAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetLogResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("isAuthenticated()")
public class GetLogHandler extends AbstractActionHandler<GetLogAction, GetLogResult> {

    @Autowired
    private LogEntryService logEntryService;

    public GetLogHandler() {
        super(GetLogAction.class);
    }

    @Override
    public GetLogResult execute(GetLogAction action, ExecutionContext context) throws ActionException {
        Logger logger = new Logger();
        for (GetLogAction.PairLogLevelMessage message : action.getMessages()) {
            switch (message.getLogLevel()) {
                case INFO: {
                    logger.info(message.getMessage());
                    break;
                }
                case WARN: {
                    logger.warn(message.getMessage());
                    break;
                }
                case ERROR: {
                    logger.warn(message.getMessage());
                    break;
                }
            }
        }
        GetLogResult result = new GetLogResult();
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(GetLogAction action, GetLogResult result, ExecutionContext context) throws ActionException {

    }
}
