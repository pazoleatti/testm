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
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetLastArchiveDateHandler extends AbstractActionHandler<GetLastArchiveDateAction, GetLastArchiveDateResult> {

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };

    @Autowired
    AuditService auditService;

    public GetLastArchiveDateHandler() {
        super(GetLastArchiveDateAction.class);
    }

    @Override
    public GetLastArchiveDateResult execute(GetLastArchiveDateAction action, ExecutionContext executionContext) throws ActionException {
        GetLastArchiveDateResult result = new GetLastArchiveDateResult();
        Date lastArchiveDate = auditService.getLastArchiveDate();
        result.setLastArchiveDate(lastArchiveDate != null? sdf.get().format(lastArchiveDate) : "");
        return result;
    }

    @Override
    public void undo(GetLastArchiveDateAction action, GetLastArchiveDateResult result, ExecutionContext executionContext) throws ActionException {

    }
}
