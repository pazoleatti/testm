package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationList;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationListHandler extends AbstractActionHandler<GetDeclarationList, GetDeclarationListResult> {

	public GetDeclarationListHandler() {
		super(GetDeclarationList.class);
	}

	@Autowired
	private DeclarationDataSearchService declarationDataSearchService;

	@Override
	public GetDeclarationListResult execute(GetDeclarationList action, ExecutionContext executionContext) throws ActionException {
		if(action == null || action.getDeclarationFilter() == null){
			return null;
		}
		PagingResult<DeclarationDataSearchResultItem> page = declarationDataSearchService.search(action.getDeclarationFilter());
		GetDeclarationListResult result = new GetDeclarationListResult();
		result.setRecords(page.getRecords());
		result.setTotalCountOfRecords(page.getTotalRecordCount());
		return result;
	}

	@Override
	public void undo(GetDeclarationList getDeclarationList, GetDeclarationListResult getDeclarationListResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
