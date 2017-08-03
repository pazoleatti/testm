package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationList;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationListResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationListStateAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationListStateResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class GetDeclarationListStateHandler extends AbstractActionHandler<GetDeclarationListStateAction, GetDeclarationListStateResult> {

	public GetDeclarationListStateHandler() {
		super(GetDeclarationListStateAction.class);
	}

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private SecurityService securityService;

	@Override
	public GetDeclarationListStateResult execute(GetDeclarationListStateAction action, ExecutionContext executionContext) throws ActionException {
        GetDeclarationListStateResult result = new GetDeclarationListStateResult();
        Map<Long, State> stateMap = new HashMap<Long, State>();
        TAUserInfo taUserInfo = securityService.currentUserInfo();
        for(Long declarationId: action.getDeclarationIds()) {
            if (declarationDataService.existDeclarationData(declarationId)) {
                stateMap.put(declarationId, declarationDataService.get(declarationId, taUserInfo).getState());
            }
        }
        result.setStateMap(stateMap);
		return result;
	}

	@Override
	public void undo(GetDeclarationListStateAction action, GetDeclarationListStateResult result, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
