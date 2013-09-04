package com.aplana.sbrf.taxaccounting.web.module.migration.server;

import com.aplana.sbrf.taxaccounting.service.MappingService;
import com.aplana.sbrf.taxaccounting.service.MessageService;
import com.aplana.sbrf.taxaccounting.service.MigrationService;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationAction;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class MigrationHandler extends AbstractActionHandler<MigrationAction, MigrationResult> {

    @Autowired
    MigrationService migrationService;

    @Autowired
    MappingService mappingService;

    // EJB-модуль отправки JMS-сообщений
    @Autowired
    @Qualifier("messageService")
    private MessageService messageService;

    public MigrationHandler() {
        super(MigrationAction.class);
    }

    @Override
    public MigrationResult execute(MigrationAction action, ExecutionContext executionContext)
            throws ActionException {
        MigrationResult result = new MigrationResult();
        result.setExemplarList(migrationService.getActualExemplarByRnuType(action.getRnus()));
        Map<String, byte[]> map = migrationService.getFiles(action.getRnus());

//        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
//            mappingService.addFormData(entry.getKey(), entry.getValue());
//        }

        result.setSenFilesCount(messageService.sendFiles(map));
        return result;
    }

    @Override
    public void undo(MigrationAction action, MigrationResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
