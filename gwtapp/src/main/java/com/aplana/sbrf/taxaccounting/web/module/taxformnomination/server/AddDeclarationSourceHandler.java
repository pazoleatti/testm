package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AddDeclarationSourceAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AddDeclarationSourceResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class AddDeclarationSourceHandler extends AbstractActionHandler<AddDeclarationSourceAction, AddDeclarationSourceResult> {{
}

	public AddDeclarationSourceHandler() {
		super(AddDeclarationSourceAction.class);
	}

	@Autowired
	SourceService departmentFormTypeService;
	@Autowired
	DepartmentService departmentService;
	@Autowired
	DeclarationTypeService declarationTypeService;
	@Autowired
	LogEntryService logEntryService;

	@Override
	public AddDeclarationSourceResult execute(AddDeclarationSourceAction action, ExecutionContext executionContext) throws ActionException {
		List<LogEntry> logs = new ArrayList<LogEntry>();
        boolean detectRelations = false;

        for (Integer depId : action.getDepartmentId()) {
			for (Long dt : action.getDeclarationTypeId()) {
				boolean canAssign = true;
                //TODO тоже надо откуда то брать период
				for (DepartmentDeclarationType ddt : departmentFormTypeService.getDDTByDepartment(depId.intValue(), action.getTaxType(), new Date(), new Date())) {
					if (ddt.getDeclarationTypeId() == dt) {
                        detectRelations = true;
						canAssign = false;
						logs.add(new LogEntry(LogLevel.WARNING, "Для \"" + departmentService.getDepartment(depId).getName() +
								"\" уже существует назначение \"" + declarationTypeService.get(ddt.getDeclarationTypeId()).getName() + "\""));
					}
				}
				if (canAssign) {
					departmentFormTypeService.saveDDT((long)depId, dt.intValue(), action.getPerformers());
				}
			}
		}
		AddDeclarationSourceResult result = new AddDeclarationSourceResult();
		result.setUuid(logEntryService.save(logs));
        result.setIssetRelations(detectRelations);
        return result;
	}

	@Override
	public void undo(AddDeclarationSourceAction addDeclarationSourceAction, AddDeclarationSourceResult addDeclarationSourceResult, ExecutionContext executionContext) throws ActionException {
	}
}
