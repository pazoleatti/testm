package com.aplana.sbrf.taxaccounting.migration.web.server;

import com.aplana.sbrf.taxaccounting.migration.service.MessageService;
import com.aplana.sbrf.taxaccounting.migration.web.shared.StartAction;
import com.aplana.sbrf.taxaccounting.migration.web.shared.StartResult;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.util.Map;

@Component
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class StartHandler extends AbstractActionHandler<StartAction, StartResult> {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    MigrationService migrationService;

    @Autowired
    MessageService messageService;

    public StartHandler() {
        super(StartAction.class);
    }

    @Override
    public StartResult execute(StartAction action, ExecutionContext context) throws ActionException {
        StartResult result = new StartResult();
        result.getExemplarList().addAll(migrationService.getActualExemplarByRnuType(action.getRnuList()));
        //Map<String, String> map = migrationService.startMigrationProcessDebug(action.getRnuList());
        //result.setFiles(map);

        Map<String, byte[]> map = migrationService.startMigrationProcess(action.getRnuList());
        try {
            messageService.sendMessagePack(map);
        } catch (JMSException e) {
            logger.error("Error by sending messages with transport files data. " + e.getMessage());
        }

        return result;
    }

    @Override
    public void undo(StartAction action, StartResult result, ExecutionContext context) throws ActionException {
    }
}
