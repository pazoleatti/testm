package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.exception.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterData;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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
	private DeclarationService declarationService;

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Override
	public GetDeclarationFilterDataResult execute(GetDeclarationFilterData action, ExecutionContext executionContext) throws ActionException {
		GetDeclarationFilterDataResult res = new GetDeclarationFilterDataResult();
		DeclarationFilterAvailableValues declarationFilterValues = declarationService.getFilterAvailableValues(securityService
				.currentUser().getId(), action.getTaxType());

		res.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(declarationFilterValues
				.getDepartmentIds()).values()));
		res.setTaxPeriods(taxPeriodDao.listByTaxType(action.getTaxType()));
		res.setFilterValues(declarationFilterValues);
		res.setCurrentReportPeriod(getCurrentReportPeriod(action.getTaxType()));
		return res;
	}

	@Override
	public void undo(GetDeclarationFilterData getDeclarationFilterData, GetDeclarationFilterDataResult getDeclarationFilterDataResult, ExecutionContext executionContext) throws ActionException {
		//Do nothing
	}

	private ReportPeriod getCurrentReportPeriod(TaxType taxType){
		try {
			ReportPeriod rp = reportPeriodDao.getCurrentPeriod(taxType);
			if (rp != null) {
				return rp;
			}
		} catch (DaoException e) {
			logger.warn("Failed to find current report period for taxType = " + taxType + ", message is: " + e.getMessage());
		}
		return null;
	}
}
