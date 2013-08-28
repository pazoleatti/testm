package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler.DeparmentFormTypeAssembler;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentSourcesResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetCurrentSourcesForFormAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetCurrentSourcesForFormHandler
		extends
		AbstractActionHandler<GetCurrentSourcesForFormAction, GetCurrentSourcesResult> {

	@Autowired
	private SourceService departmentFormTypeService;

	@Autowired
	private DeparmentFormTypeAssembler deparmentFormTypeAssembler;

	public GetCurrentSourcesForFormHandler() {
		super(GetCurrentSourcesForFormAction.class);
	}

	@Override
	public GetCurrentSourcesResult execute(GetCurrentSourcesForFormAction action,
			ExecutionContext context) throws ActionException {
		GetCurrentSourcesResult result = new GetCurrentSourcesResult();
		List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
				.getDFTSourcesByDFT(action.getDepartmentId(),
						action.getFormTypeId(), action.getKind());
		result.setCurrentSources(deparmentFormTypeAssembler
				.assemble(departmentFormTypes));
		return result;
	}

	@Override
	public void undo(GetCurrentSourcesForFormAction action,
			GetCurrentSourcesResult result, ExecutionContext context)
			throws ActionException {
		// Nothing!
	}

}
