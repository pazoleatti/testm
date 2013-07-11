package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetFormReceiversAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetFormReceiversResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetFormReceiversHandler extends AbstractActionHandler<GetFormReceiversAction, GetFormReceiversResult> {

	@Autowired
	private DepartmentFormTypeService departmentFormTypeService;

	@Autowired
	private FormTypeDao formTypeDao;

    public GetFormReceiversHandler() {
        super(GetFormReceiversAction.class);
    }

    @Override
    public GetFormReceiversResult execute(GetFormReceiversAction action, ExecutionContext context) throws ActionException {
		GetFormReceiversResult result = new GetFormReceiversResult();
		List<DepartmentFormType> departmentFormTypes =
				departmentFormTypeService.getDepartmentFormDestinations(action.getDepartmentId(), action.getTaxType());
		result.setReceiversDepartmentFormTypes(departmentFormTypes);

		Map<Integer, String> formTypeNames = new HashMap<Integer, String>();
		for (DepartmentFormType departmentFormType : departmentFormTypes) {
			formTypeNames.put(departmentFormType.getFormTypeId(),
					formTypeDao.getType(departmentFormType.getFormTypeId()).getName());
		}

		result.setFormTypeNames(formTypeNames);

		return result;
    }

    @Override
    public void undo(GetFormReceiversAction action, GetFormReceiversResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
