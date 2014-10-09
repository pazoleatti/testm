package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetLastArchiveDateAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetLastArchiveDateResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS', 'ROLE_OPER')")
public class GetLastArchiveDateHandler extends AbstractActionHandler<GetLastArchiveDateAction, GetLastArchiveDateResult> {

    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");

    @Autowired
    AuditService auditService;

    public GetLastArchiveDateHandler() {
        super(GetLastArchiveDateAction.class);
    }

    @Override
    public GetLastArchiveDateResult execute(GetLastArchiveDateAction action, ExecutionContext executionContext) throws ActionException {
        GetLastArchiveDateResult result = new GetLastArchiveDateResult();
        Date lastArchiveDate = auditService.getLastArchiveDate();
        result.setLastArchiveDate(lastArchiveDate != null? sdf.format(lastArchiveDate) : "");
        return result;
    }

    @Override
    public void undo(GetLastArchiveDateAction action, GetLastArchiveDateResult result, ExecutionContext executionContext) throws ActionException {

    }
}
