package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetDeclarationTypeForCreateHandler extends AbstractActionHandler<GetDeclarationTypeAction, GetDeclarationTypeResult> {

	public GetDeclarationTypeForCreateHandler() {
		super(GetDeclarationTypeAction.class);
	}

	@Autowired
	DeclarationTypeService declarationTypeService;

	@Override
	public GetDeclarationTypeResult execute(GetDeclarationTypeAction action, ExecutionContext executionContext) throws ActionException {
		GetDeclarationTypeResult result = new GetDeclarationTypeResult();
		result.setDeclarationTypes(declarationTypeService.getTypes(action.getDepartmentId(), action.getReportPeriod(), action.getTaxType()));

		return result;
	}

	@Override
	public void undo(GetDeclarationTypeAction getDeclarationTypeAction, GetDeclarationTypeResult getDeclarationTypeResult, ExecutionContext executionContext) throws ActionException {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
