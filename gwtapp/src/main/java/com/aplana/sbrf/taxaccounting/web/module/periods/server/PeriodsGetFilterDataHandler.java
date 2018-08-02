package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.PeriodsGetFilterDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS')")
public class PeriodsGetFilterDataHandler extends AbstractActionHandler<PeriodsGetFilterData, PeriodsGetFilterDataResult> {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private PeriodService reportPeriodService;

    public PeriodsGetFilterDataHandler() {
        super(PeriodsGetFilterData.class);
    }

    @Override
    public PeriodsGetFilterDataResult execute(PeriodsGetFilterData action, ExecutionContext executionContext) throws ActionException {
	    PeriodsGetFilterDataResult res = new PeriodsGetFilterDataResult();
	    TAUserInfo userInfo = securityService.currentUserInfo();
	    res.setTaxType(action.getTaxType());

        TaxType taxType = action.getTaxType();
        Set<Integer> ad = new HashSet<Integer>();
        if (userInfo.getUser().hasRole(taxType, TARole.N_ROLE_CONTROL_UNP)) {
	        res.setCanEdit(true);
            switch (taxType) {
                case NDFL:
                    res.setCanChangeDepartment(false);
	                res.setDepartments(Arrays.asList(departmentService.getBankDepartment()));
	                res.setSelectedDepartment(
			                new DepartmentPair(res.getDepartments().get(0).getId(),
					                res.getDepartments().get(0).getParentId(),
					                res.getDepartments().get(0).getName())
	                );
                    break;
                default:
                    break;
            }
        } else if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_OPER, TARole.N_ROLE_CONTROL_NS)) {
	        res.setCanChangeDepartment(false);
			res.setDepartments(Arrays.asList(departmentService.getBankDepartment()));
			res.setSelectedDepartment(
					new DepartmentPair(res.getDepartments().get(0).getId(),
							res.getDepartments().get(0).getParentId(),
							res.getDepartments().get(0).getName())
			);
	        switch (taxType) {
                case NDFL:
			        res.setCanEdit(false);
			        break;
	        }
        }
	    if (ad.isEmpty()) { // default all available
            for (Department dep : res.getDepartments()) {
                ad.add(dep.getId());
            }
        }
	    res.setAvalDepartments(ad);
	    Calendar current = Calendar.getInstance();
		res.setYearFrom(current.get(Calendar.YEAR));
		res.setYearTo(current.get(Calendar.YEAR));
		res.setCurrentYear(current.get(Calendar.YEAR));

		return res;
    }

    @Override
    public void undo(PeriodsGetFilterData getFilterData, PeriodsGetFilterDataResult getFilterDataResult,
                     ExecutionContext executionContext) throws ActionException {
        //ничего не делаем
    }
}
