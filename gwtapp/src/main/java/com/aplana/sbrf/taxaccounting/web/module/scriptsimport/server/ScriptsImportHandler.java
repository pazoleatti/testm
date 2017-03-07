package com.aplana.sbrf.taxaccounting.web.module.scriptsimport.server;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ScriptExecutionService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.scriptsimport.shared.ScriptsImportAction;
import com.aplana.sbrf.taxaccounting.web.module.scriptsimport.shared.ScriptsImportResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Обработка загружаемых данных скриптов
 * User: Denis Loshkarev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class ScriptsImportHandler extends AbstractActionHandler<ScriptsImportAction, ScriptsImportResult> {
    @Autowired
    BlobDataService blobDataService;
    @Autowired
    SecurityService securityService;
    @Autowired
    ScriptExecutionService scriptExecutionService;
    @Autowired
    LogEntryService logEntryService;

    public ScriptsImportHandler() {
        super(ScriptsImportAction.class);
    }

    @Override
    public ScriptsImportResult execute(ScriptsImportAction importAction, ExecutionContext executionContext) throws ActionException {
        BlobData blobData = blobDataService.get(importAction.getUuid());
        ScriptsImportResult result = new ScriptsImportResult();
        Logger logger = new Logger();
        TAUserInfo userInfo = securityService.currentUserInfo();
        try {
            scriptExecutionService.importScripts(logger, blobData.getInputStream(), blobData.getName(), userInfo);
        } catch (Exception e) {} finally {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public void undo(ScriptsImportAction importAction, ScriptsImportResult importResult, ExecutionContext executionContext) throws ActionException {
        // Не обрабатывается
    }
}
