package com.aplana.sbrf.taxaccounting.migration.web.server;

import com.aplana.sbrf.taxaccounting.migration.web.shared.StartAction;
import com.aplana.sbrf.taxaccounting.migration.web.shared.StartResult;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class StartHandler extends AbstractActionHandler<StartAction, StartResult> {

    @Autowired
    MigrationService migrationService;

    public StartHandler() {
        super(StartAction.class);
    }

    @Override
    public StartResult execute(StartAction action, ExecutionContext context) throws ActionException {
        StartResult result = new StartResult();
        result.getExemplarList().addAll(migrationService.getActualExemplarByRnuType(action.getRnuList()));
        result.setFiles(migrationService.startMigrationProcess(action.getRnuList()));
        return result;
    }

    @Override
    public void undo(StartAction action, StartResult result, ExecutionContext context) throws ActionException {

    }
}
