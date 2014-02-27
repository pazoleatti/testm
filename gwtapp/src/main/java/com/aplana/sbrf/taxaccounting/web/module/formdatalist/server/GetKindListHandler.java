package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.service.FormDataAccessService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetKindListAction;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetKindListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author fmukhametdinov
 */
@Component
public class GetKindListHandler extends AbstractActionHandler<GetKindListAction, GetKindListResult> {

    @Autowired
    SecurityService securityService;

    @Autowired
    FormDataAccessService dataAccessService;

    public GetKindListHandler() {
        super(GetKindListAction.class);
    }

    @Override
    public GetKindListResult execute(GetKindListAction action, ExecutionContext context) throws ActionException {

        GetKindListResult result = new GetKindListResult();

        List<FormDataKind> kinds = new ArrayList<FormDataKind>(FormDataKind.values().length);

        kinds.addAll(dataAccessService.getAvailableFormDataKind(securityService.currentUserInfo(), asList(action.getTaxType())));

        result.setDataKinds(kinds);

        return result;
    }

    @Override
    public void undo(GetKindListAction action, GetKindListResult result, ExecutionContext context) throws ActionException {

    }
}
