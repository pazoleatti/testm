package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler.DepartmentDeclarationTypeComparator;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDeclarationReceiversAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDeclarationReceiversResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationReceiversHandler
		extends AbstractActionHandler<GetDeclarationReceiversAction, GetDeclarationReceiversResult> {

	@Autowired
	private SourceService sourceService;

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
				sourceService.getDDTByDepartment(action.getDepartmentId(), action.getTaxType());

		// Требование - не отображать декларации обособленных подразделений удалено из постановки 
		// http://conf.aplana.com/pages/viewpage.action?pageId=9583288&focusedCommentId=9597316#comment-9597316
		// http://jira.aplana.com/browse/SBRFACCTAX-3320

		Map<Integer, DeclarationType> declarationTypes = new HashMap<Integer, DeclarationType>();
		for (DepartmentDeclarationType receiver : receivers) {
			declarationTypes.put(receiver.getDeclarationTypeId(),
					sourceService.getDeclarationType(receiver.getDeclarationTypeId()));
		}
		result.setDeclarationTypes(declarationTypes);

		Collections.sort(receivers, new DepartmentDeclarationTypeComparator(declarationTypes));
		result.setDeclarationReceivers(receivers);

		return result;
    }

    @Override
    public void undo(GetDeclarationReceiversAction action, GetDeclarationReceiversResult result, ExecutionContext context)
			throws ActionException {
        // Nothing!
    }
}
