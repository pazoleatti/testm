package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterData;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationFilterDataHandler extends AbstractActionHandler<GetDeclarationFilterData, GetDeclarationFilterDataResult> {

	public GetDeclarationFilterDataHandler() {
		super(GetDeclarationFilterData.class);
	}

	private Log logger = LogFactory.getLog(getClass());

	@Autowired
	private SecurityService securityService;

	@Autowired
	private TaxPeriodDao taxPeriodDao;

	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private DeclarationDataSearchService declarationDataSearchService;	

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Override
	public GetDeclarationFilterDataResult execute(GetDeclarationFilterData action, ExecutionContext executionContext) throws ActionException {
		GetDeclarationFilterDataResult res = new GetDeclarationFilterDataResult();
		DeclarationDataFilterAvailableValues declarationFilterValues =
				declarationDataSearchService.getFilterAvailableValues(securityService.currentUserInfo(), action.getTaxType());

		res.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(declarationFilterValues
				.getDepartmentIds()).values()));
		res.setTaxPeriods(taxPeriodDao.listByTaxType(action.getTaxType()));
		res.setFilterValues(declarationFilterValues);
		res.setCurrentReportPeriod(null);
		return res;
	}

	@Override
	public void undo(GetDeclarationFilterData getDeclarationFilterData, GetDeclarationFilterDataResult getDeclarationFilterDataResult, ExecutionContext executionContext) throws ActionException {
		//Do nothing
	}

}
