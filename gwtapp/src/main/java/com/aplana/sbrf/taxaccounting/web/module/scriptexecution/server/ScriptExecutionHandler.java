package com.aplana.sbrf.taxaccounting.web.module.scriptexecution.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ScriptExecutionService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.scriptexecution.shared.ScriptExecutionAction;
import com.aplana.sbrf.taxaccounting.web.module.scriptexecution.shared.ScriptExecutionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Хэндлер модуля выполнения скриптов из конфигуратора
 * @author Stanislav Yasinskiy
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class ScriptExecutionHandler extends AbstractActionHandler<ScriptExecutionAction, ScriptExecutionResult> {

    @Autowired
    private ScriptExecutionService scriptExecutionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    public ScriptExecutionHandler() {
        super(ScriptExecutionAction.class);
    }

    @Override
    public ScriptExecutionResult execute(ScriptExecutionAction action, ExecutionContext context) throws ActionException {
        Logger logger = new Logger();

        scriptExecutionService.executeScript(securityService.currentUserInfo(), action.getScript(), logger);

        ScriptExecutionResult result = new ScriptExecutionResult();
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(ScriptExecutionAction action, ScriptExecutionResult result, ExecutionContext context) throws ActionException {
    }
}