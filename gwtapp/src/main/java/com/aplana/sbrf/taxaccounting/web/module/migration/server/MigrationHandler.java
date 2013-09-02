package com.aplana.sbrf.taxaccounting.web.module.migration.server;

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

    private static long[] rnus = {25L, 26L, 27L, 31L, 51L, 53L, 54L, 59L, 60L, 64L};

    @Autowired
    MigrationService migrationService;

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
        result.setExemplarList(migrationService.getActualExemplarByRnuType(rnus));
        Map<String, byte[]> map = migrationService.getFiles(rnus);
        result.setSenFilesCount(messageService.sendFiles(map));
        return result;
    }

    @Override
    public void undo(MigrationAction action, MigrationResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
