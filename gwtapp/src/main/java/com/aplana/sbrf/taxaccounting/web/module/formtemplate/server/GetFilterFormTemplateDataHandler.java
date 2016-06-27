package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFilterFormTemplateData;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFilterFormTemplateDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONF')")
public class GetFilterFormTemplateDataHandler extends AbstractActionHandler<GetFilterFormTemplateData, GetFilterFormTemplateDataResult> {
	
    public GetFilterFormTemplateDataHandler() {
        super(GetFilterFormTemplateData.class);
    }

    @Override
    public GetFilterFormTemplateDataResult execute(GetFilterFormTemplateData action, ExecutionContext executionContext) throws ActionException {
        GetFilterFormTemplateDataResult res = new GetFilterFormTemplateDataResult();
	    
        List<TaxType> taxTypes = new ArrayList<TaxType>();
        taxTypes.add(TaxType.VAT);
        taxTypes.add(TaxType.DEAL);
        taxTypes.add(TaxType.INCOME);
        taxTypes.add(TaxType.PROPERTY);
        taxTypes.add(TaxType.TRANSPORT);
        taxTypes.add(TaxType.MARKET);
        taxTypes.add(TaxType.ETR);

        res.setTaxTypes(taxTypes);
        return res;
    }

    @Override
    public void undo(GetFilterFormTemplateData getFilterData, GetFilterFormTemplateDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }

}
