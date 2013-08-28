package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler.DeparmentFormTypeAssembler;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentSourcesForDeclaratonAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetCurrentSourcesForDeclaratonHandler
		extends
		AbstractActionHandler<GetCurrentSourcesForDeclaratonAction, GetCurrentSourcesResult> {

	@Autowired
	private SourceService departmentFormTypeService;

	@Autowired
	private DeparmentFormTypeAssembler deparmentFormTypeAssembler;

	public GetCurrentSourcesForDeclaratonHandler() {
		super(GetCurrentSourcesForDeclaratonAction.class);
	}

	@Override
	public GetCurrentSourcesResult execute(
			GetCurrentSourcesForDeclaratonAction action, ExecutionContext context)
			throws ActionException {
		GetCurrentSourcesResult result = new GetCurrentSourcesResult();
		List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
				.getDFTSourceByDDT(action.getDepartmentId(),
						action.getDeclarationTypeId());
		result.setCurrentSources(deparmentFormTypeAssembler
				.assemble(departmentFormTypes));
		return result;
	}

	@Override
	public void undo(GetCurrentSourcesForDeclaratonAction arg0,
			GetCurrentSourcesResult arg1, ExecutionContext arg2)
			throws ActionException {
		// Nothing!
	}

}
