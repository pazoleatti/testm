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
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler.DepartmentFormTypeComparator;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetFormDFTAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetFormDFTResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFormDFTHandler extends AbstractActionHandler<GetFormDFTAction, GetFormDFTResult> {

	@Autowired
	private SourceService sourceService;
	

    public GetFormDFTHandler() {
        super(GetFormDFTAction.class);
    }

    @Override
    public GetFormDFTResult execute(GetFormDFTAction action, ExecutionContext context) throws ActionException {
		GetFormDFTResult result = new GetFormDFTResult();
		List<DepartmentFormType> receivers =
				sourceService.getDFTByDepartment(action.getDepartmentId(), action.getTaxType());

		Map<Integer, FormType> formTypes = new HashMap<Integer, FormType>();
		for (DepartmentFormType departmentFormType : receivers) {
			formTypes.put(departmentFormType.getFormTypeId(),
					sourceService.getFormType(departmentFormType.getFormTypeId()));
		}
		result.setFormTypes(formTypes);

		Collections.sort(receivers, new DepartmentFormTypeComparator(formTypes));
		result.setDepartmentFormTypes(receivers);

		return result;
    }

    @Override
    public void undo(GetFormDFTAction action, GetFormDFTResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
