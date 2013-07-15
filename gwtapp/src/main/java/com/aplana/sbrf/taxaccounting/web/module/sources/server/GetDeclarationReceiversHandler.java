package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentDeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDeclarationReceiversAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDeclarationReceiversResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationReceiversHandler
		extends AbstractActionHandler<GetDeclarationReceiversAction, GetDeclarationReceiversResult> {

	@Autowired
	private DepartmentDeclarationTypeService departmentDeclarationTypeService;

	@Autowired
	private DeclarationTypeDao declarationTypeDao;

	@Autowired
	private DepartmentService departmentService;

    public GetDeclarationReceiversHandler() {
        super(GetDeclarationReceiversAction.class);
    }

    @Override
    public GetDeclarationReceiversResult execute(GetDeclarationReceiversAction action, ExecutionContext context)
			throws ActionException {
		GetDeclarationReceiversResult result = new GetDeclarationReceiversResult();
		List<DepartmentDeclarationType> receivers =
				departmentDeclarationTypeService.getByTaxType(action.getDepartmentId(), action.getTaxType());

		// для декларации ОП (по налогу на прибыль) отсутствует механизм источники-приёмники —
		// данный вид декларации в таблице "Приёмники" не отображается
		if (TaxType.INCOME.equals(action.getTaxType())) {
			List<Department> isolatedDepartments = departmentService.getIsolatedDepartments();
			Set<Integer> ids = new HashSet<Integer>(isolatedDepartments.size());
			for (Department department : isolatedDepartments) {
				ids.add(department.getId());
			}

			Iterator<DepartmentDeclarationType> iterator = receivers.iterator();
			while (iterator.hasNext()) {
				if (ids.contains(iterator.next().getDepartmentId())) {
					iterator.remove();
				}
			}
		}

		result.setDeclarationReceivers(receivers);

		Map<Integer, DeclarationType> declarationTypes = new HashMap<Integer, DeclarationType>();
		for (DepartmentDeclarationType receiver : receivers) {
			declarationTypes.put(receiver.getDeclarationTypeId(),
					declarationTypeDao.get(receiver.getDeclarationTypeId()));
		}

		result.setDeclarationTypes(declarationTypes);

		return result;
    }

    @Override
    public void undo(GetDeclarationReceiversAction action, GetDeclarationReceiversResult result, ExecutionContext context)
			throws ActionException {
        // Nothing!
    }
}
