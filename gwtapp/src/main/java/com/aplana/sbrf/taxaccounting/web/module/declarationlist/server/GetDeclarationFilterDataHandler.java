package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterData;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationFilterDataHandler extends AbstractActionHandler<GetDeclarationFilterData, GetDeclarationFilterDataResult> {

	public GetDeclarationFilterDataHandler() {
		super(GetDeclarationFilterData.class);
	}

	@Autowired
	private FormDataSearchService formDataSearchService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	TaxPeriodDao taxPeriodDao;

	@Override
	public GetDeclarationFilterDataResult execute(GetDeclarationFilterData action, ExecutionContext executionContext) throws ActionException {
		GetDeclarationFilterDataResult res = new GetDeclarationFilterDataResult();
		if (securityService.currentUser().hasRole("ROLE_CONTROL")){
			//TODO: НЕ ДОДЕЛАНО! ПРОКОНСУЛЬТИРОВАТЬСЯ!
			res.setDepartments(formDataSearchService.listAllDepartmentsByParentDepartmentId(securityService.currentUser()
					.getDepartmentId()));
		} else if (securityService.currentUser().hasRole("ROLE_CONTROL_UNP")){
			//TODO: НЕ ДОДЕЛАНО! ПРОКОНСУЛЬТИРОВАТЬСЯ!
			res.setDepartments(formDataSearchService.listAllDepartmentsByParentDepartmentId(securityService.currentUser()
					.getDepartmentId()));
		}

		res.setTaxPeriods(taxPeriodDao.listByTaxType(action.getTaxType()));
		return res;
	}

	@Override
	public void undo(GetDeclarationFilterData getDeclarationFilterData, GetDeclarationFilterDataResult getDeclarationFilterDataResult, ExecutionContext executionContext) throws ActionException {
		//Do nothing
	}
}
