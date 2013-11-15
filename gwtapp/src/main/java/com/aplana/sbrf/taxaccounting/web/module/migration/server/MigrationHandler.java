package com.aplana.sbrf.taxaccounting.web.module.migration.server;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.MessageService;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationAction;
import com.aplana.sbrf.taxaccounting.web.module.migration.shared.MigrationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class MigrationHandler extends AbstractActionHandler<MigrationAction, MigrationResult> {

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
        // Отправка файлов
        MigrationResult result = new MigrationResult();
        try {
            result.setResult(messageService.sendFiles());
        } catch (ServiceException ex) {
            throw new ActionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            String msg = "Ошибка отправки транспортных файлов JMS-сообщениями.";
            throw new ActionException(msg, ex);
        }
        return result;
    }

    @Override
    public void undo(MigrationAction action, MigrationResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
