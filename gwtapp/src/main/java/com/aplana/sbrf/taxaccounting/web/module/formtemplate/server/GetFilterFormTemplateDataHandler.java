package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFilterFormTemplateData;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFilterFormTemplateDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetFilterFormTemplateDataHandler extends AbstractActionHandler<GetFilterFormTemplateData, GetFilterFormTemplateDataResult> {

    @Autowired
    private SecurityService securityService;

    public GetFilterFormTemplateDataHandler() {
        super(GetFilterFormTemplateData.class);
    }

    @Override
    public GetFilterFormTemplateDataResult execute(GetFilterFormTemplateData action, ExecutionContext executionContext) throws ActionException {
        GetFilterFormTemplateDataResult res = new GetFilterFormTemplateDataResult();
        TAUser user = securityService.currentUserInfo().getUser();
        List<TaxType> taxTypes = new ArrayList<TaxType>();
        if (user.hasRole(TARole.N_ROLE_CONF)) {
            taxTypes.add(TaxType.NDFL);
        }
        if (user.hasRole(TARole.F_ROLE_CONF)) {
            taxTypes.add(TaxType.PFR);
        }

        res.setTaxTypes(taxTypes);
        return res;
    }

    @Override
    public void undo(GetFilterFormTemplateData getFilterData, GetFilterFormTemplateDataResult getFilterDataResult, ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }

}
