package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.comparators.DepartmentFormTypeComparator;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDeclarationReceiverSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDeclarationReceiverSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationReceiverSourcesHandler extends AbstractActionHandler<GetDeclarationReceiverSourcesAction,
		GetDeclarationReceiverSourcesResult> {

	@Autowired
	private DepartmentFormTypeService departmentFormTypeService;


    public GetDeclarationReceiverSourcesHandler() {
        super(GetDeclarationReceiverSourcesAction.class);
    }

    @Override
    public GetDeclarationReceiverSourcesResult execute(GetDeclarationReceiverSourcesAction action, ExecutionContext context) throws ActionException {
		GetDeclarationReceiverSourcesResult result = new GetDeclarationReceiverSourcesResult();
		List<DepartmentFormType> departmentFormTypes =
				departmentFormTypeService.getDeclarationSources(action.getDepartmentId(), action.getDeclarationTypeId());

		Map<Integer, FormType> formTypes = new HashMap<Integer, FormType>();
		for (DepartmentFormType departmentFormType : departmentFormTypes) {
			formTypes.put(departmentFormType.getFormTypeId(),
					departmentFormTypeService.getFormType(departmentFormType.getFormTypeId()));
		}
		result.setFormTypes(formTypes);

		Collections.sort(departmentFormTypes, new DepartmentFormTypeComparator(formTypes));
		result.setFormReceiverSources(departmentFormTypes);

		return result;
    }

    @Override
    public void undo(GetDeclarationReceiverSourcesAction action, GetDeclarationReceiverSourcesResult result, ExecutionContext context)
			throws ActionException {
        // Nothing!
    }
}
