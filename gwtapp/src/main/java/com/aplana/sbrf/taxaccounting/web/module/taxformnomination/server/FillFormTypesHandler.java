package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.FillFormTypesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.FillFormTypesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author vpetrov
 */

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class FillFormTypesHandler extends AbstractActionHandler<FillFormTypesAction, FillFormTypesResult> {

    @Autowired
    private FormDataAccessService dataAccessService;

    @Autowired
    private SecurityService securityService;

    public FillFormTypesHandler() {
        super(FillFormTypesAction.class);
    }

    @Override
    public FillFormTypesResult execute(FillFormTypesAction action, ExecutionContext executionContext) throws ActionException {
        List<FormDataKind> kinds = new ArrayList<FormDataKind>(FormDataKind.values().length);
        kinds.addAll(dataAccessService.getAvailableFormDataKind(securityService.currentUserInfo(),asList(action.getTaxType())));
        FillFormTypesResult result = new FillFormTypesResult();
        result.setTaxType(action.getTaxType());
        result.setFormTypes(kinds);
        return result;
    }

    @Override
    public void undo(FillFormTypesAction fillFormTypesAction, FillFormTypesResult fillFormTypesResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
