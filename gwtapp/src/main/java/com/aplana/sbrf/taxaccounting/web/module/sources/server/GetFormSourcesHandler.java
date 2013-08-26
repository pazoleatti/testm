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
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetFormSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetFormSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFormSourcesHandler extends AbstractActionHandler<GetFormSourcesAction, GetFormSourcesResult> {

	@Autowired
	private DepartmentFormTypeService departmentFormTypeService;

    public GetFormSourcesHandler() {
        super(GetFormSourcesAction.class);
    }

    @Override
    public GetFormSourcesResult execute(GetFormSourcesAction action, ExecutionContext context) throws ActionException {
		GetFormSourcesResult result = new GetFormSourcesResult();
		List<DepartmentFormType> sources =
				departmentFormTypeService.getDepartmentFormSources(action.getDepartmentId(), action.getTaxType());

		Map<Integer, FormType> formTypes = new HashMap<Integer, FormType>();
		for (DepartmentFormType departmentFormType : sources) {
			formTypes.put(departmentFormType.getFormTypeId(),
					departmentFormTypeService.getFormType(departmentFormType.getFormTypeId()));
		}
		result.setFormTypes(formTypes);

		Collections.sort(sources, new DepartmentFormTypeComparator(formTypes));
		result.setFormSources(sources);

		return result;
    }

    @Override
    public void undo(GetFormSourcesAction action, GetFormSourcesResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
