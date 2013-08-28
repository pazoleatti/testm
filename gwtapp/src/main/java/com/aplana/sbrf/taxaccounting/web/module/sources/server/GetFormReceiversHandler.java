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
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetFormReceiversAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetFormReceiversResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFormReceiversHandler extends AbstractActionHandler<GetFormReceiversAction, GetFormReceiversResult> {

	@Autowired
	private SourceService departmentFormTypeService;
	

    public GetFormReceiversHandler() {
        super(GetFormReceiversAction.class);
    }

    @Override
    public GetFormReceiversResult execute(GetFormReceiversAction action, ExecutionContext context) throws ActionException {
		GetFormReceiversResult result = new GetFormReceiversResult();
		List<DepartmentFormType> receivers =
				departmentFormTypeService.getDFTByDepartment(action.getDepartmentId(), action.getTaxType());

		Map<Integer, FormType> formTypes = new HashMap<Integer, FormType>();
		for (DepartmentFormType departmentFormType : receivers) {
			formTypes.put(departmentFormType.getFormTypeId(),
					departmentFormTypeService.getFormType(departmentFormType.getFormTypeId()));
		}
		result.setFormTypes(formTypes);

		Collections.sort(receivers, new DepartmentFormTypeComparator(formTypes));
		result.setFormReceivers(receivers);

		return result;
    }

    @Override
    public void undo(GetFormReceiversAction action, GetFormReceiversResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
