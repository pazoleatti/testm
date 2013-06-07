package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditFilterDataAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * User: avanteev
 * Обработчик запроса для заполнения данных фильтра
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class GetAuditFilterDataHandler extends AbstractActionHandler<GetAuditFilterDataAction, GetAuditFilterDataResult> {

    @Autowired
    AuditService auditService;

    private static final List<FormDataKind> FORM_DATA_KINDS = new LinkedList<FormDataKind>();
    private static final List<TaxType> TAX_TYPES = new LinkedList<TaxType>();

    static {
        for (FormDataKind dataKind : FormDataKind.values())
            FORM_DATA_KINDS.add(dataKind);
        for (TaxType taxType : TaxType.values())
            TAX_TYPES.add(taxType);
    }

    public GetAuditFilterDataHandler() {
        super(GetAuditFilterDataAction.class);
    }

    @Override
    public GetAuditFilterDataResult execute(GetAuditFilterDataAction auditFilterDataAction, ExecutionContext executionContext) throws ActionException {
        GetAuditFilterDataResult result = new GetAuditFilterDataResult();

        LogSystemFilterAvailableValues avaliableValues = auditService.getFilterAvailableValues();
        result.setFormDataKinds(FORM_DATA_KINDS);
        result.setTaxTypes(TAX_TYPES);
        result.setAvailableValues(avaliableValues);
        return result;
    }

    @Override
    public void undo(GetAuditFilterDataAction getAuditFilterDataAction, GetAuditFilterDataResult getAuditFilterDataResult, ExecutionContext executionContext) throws ActionException {

    }
}
