package com.aplana.sbrf.taxaccounting.web.module.formsources.server;

import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.service.DepartmentFormTypeService;
import com.aplana.sbrf.taxaccounting.web.module.formsources.shared.GetFormReceiverSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.formsources.shared.GetFormReceiverSourcesResult;
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
public class GetFormReceiverSourcesHandler extends AbstractActionHandler<GetFormReceiverSourcesAction, GetFormReceiverSourcesResult> {

	@Autowired
	private DepartmentFormTypeService departmentFormTypeService;

	@Autowired
	private FormTypeDao formTypeDao;

    public GetFormReceiverSourcesHandler() {
        super(GetFormReceiverSourcesAction.class);
    }

    @Override
    public GetFormReceiverSourcesResult execute(GetFormReceiverSourcesAction action, ExecutionContext context) throws ActionException {
		GetFormReceiverSourcesResult result = new GetFormReceiverSourcesResult();
		List<DepartmentFormType> departmentFormTypes =
				departmentFormTypeService.getFormSources(action.getDepartmentId(), action.getFormTypeId(), action.getKind());
		result.setFormReceiverSources(departmentFormTypes);

		Map<Integer, FormType> formTypeNames = new HashMap<Integer, FormType>();
		for (DepartmentFormType departmentFormType : departmentFormTypes) {
			formTypeNames.put(departmentFormType.getFormTypeId(),
					formTypeDao.getType(departmentFormType.getFormTypeId()));
		}

		result.setFormTypes(formTypeNames);

		return result;
    }

    @Override
    public void undo(GetFormReceiverSourcesAction action, GetFormReceiverSourcesResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
