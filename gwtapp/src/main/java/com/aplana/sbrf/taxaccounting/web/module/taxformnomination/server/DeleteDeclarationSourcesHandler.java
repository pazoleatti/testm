package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteDeclarationSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteDeclarationSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
	@Autowired
	LogEntryService logEntryService;
	@Autowired
	DepartmentService departmentService;
	@Autowired
	FormTypeService formTypeService;
	@Autowired
	DeclarationTypeService declarationTypeService;

	@Override
	public DeleteDeclarationSourcesResult execute(DeleteDeclarationSourcesAction action, ExecutionContext executionContext) throws ActionException {
		DeleteDeclarationSourcesResult result = new DeleteDeclarationSourcesResult();
		List<LogEntry> logs = new ArrayList<LogEntry>();
		for (FormTypeKind ddt : action.getKind()) {
			List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
					.getDFTSourceByDDT(ddt.getDepartment().getId(), ddt.getFormTypeId().intValue());
			if (departmentFormTypes.isEmpty()) { // Нет назначений
				departmentFormTypeService.deleteDDT(Arrays.asList(ddt.getId()));
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Не может быть отменено назначение " +
						ddt.getDepartment().getName() + " - " + declarationTypeService.get(ddt.getFormTypeId().intValue()).getName() +
						", т.к. назначение является приемником для ");
				for (DepartmentFormType dft : departmentFormTypes) {
					sb.append(departmentService.getDepartment(dft.getDepartmentId()).getName() + " - ");
					sb.append(formTypeService.get(dft.getFormTypeId()).getName() + " - ");
					sb.append(dft.getKind().getName() + "; ");
				}

				logs.add(new LogEntry(LogLevel.ERROR, sb.toString()));
			}
		}

		result.setUuid(logEntryService.save(logs));
		return result;
	}

	@Override
	public void undo(DeleteDeclarationSourcesAction deleteDeclarationSourcesAction, DeleteDeclarationSourcesResult deleteDeclarationSourcesResult, ExecutionContext executionContext) throws ActionException {

	}
}
