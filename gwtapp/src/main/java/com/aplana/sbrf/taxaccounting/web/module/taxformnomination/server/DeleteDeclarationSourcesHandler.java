package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteDeclarationSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteDeclarationSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DeleteDeclarationSourcesHandler extends AbstractActionHandler<DeleteDeclarationSourcesAction, DeleteDeclarationSourcesResult> {

	public DeleteDeclarationSourcesHandler() {
		super(DeleteDeclarationSourcesAction.class);
	}

	@Autowired
	SourceService departmentFormTypeService;

	@Override
	public DeleteDeclarationSourcesResult execute(DeleteDeclarationSourcesAction action, ExecutionContext executionContext) throws ActionException {
		DeleteDeclarationSourcesResult result = new DeleteDeclarationSourcesResult();
		for (FormTypeKind ddt : action.getKind()) {
			List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
					.getDFTSourceByDDT(ddt.getDepartment().getId(), ddt.getId().intValue());
			if (departmentFormTypes.isEmpty()) { // Нет назначений
				departmentFormTypeService.deleteDDT(Arrays.asList(ddt.getId()));
			} else {

			}
		}

		return result;
	}

	@Override
	public void undo(DeleteDeclarationSourcesAction deleteDeclarationSourcesAction, DeleteDeclarationSourcesResult deleteDeclarationSourcesResult, ExecutionContext executionContext) throws ActionException {

	}
}
