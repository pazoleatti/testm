package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.LogSystemFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditFilterDataAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * User: avanteev
 * Обработчик запроса для заполнения данных фильтра
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetAuditFilterDataHandler extends AbstractActionHandler<GetAuditFilterDataAction, GetAuditFilterDataResult> {

    @Autowired
    AuditService auditService;
    @Autowired
    private SecurityService securityService;

    public GetAuditFilterDataHandler() {
        super(GetAuditFilterDataAction.class);
    }

    @Override
    public GetAuditFilterDataResult execute(GetAuditFilterDataAction action, ExecutionContext executionContext) throws ActionException {
        GetAuditFilterDataResult result = new GetAuditFilterDataResult();

        //TODO этот хэндлер больше не нужен. Удалить после мержа
        /*LogSystemFilterAvailableValues avaliableValues = auditService.getFilterAvailableValues(securityService.currentUserInfo().getUser());
        result.setTaxTypes(Arrays.asList(TaxType.values()));
        result.setAvailableValues(avaliableValues);*/
        return result;
    }

    @Override
    public void undo(GetAuditFilterDataAction getAuditFilterDataAction, GetAuditFilterDataResult getAuditFilterDataResult, ExecutionContext executionContext) throws ActionException {

    }
}
